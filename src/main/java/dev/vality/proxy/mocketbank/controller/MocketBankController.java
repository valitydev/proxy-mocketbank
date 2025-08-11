package dev.vality.proxy.mocketbank.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.common.exception.HellgateException;
import dev.vality.adapter.common.hellgate.HellgateClient;
import dev.vality.adapter.common.utils.CommonConverter;
import dev.vality.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import dev.vality.proxy.mocketbank.service.mpi20.constant.CallbackResponseFields;
import dev.vality.proxy.mocketbank.service.mpi20.model.CRes;
import dev.vality.proxy.mocketbank.service.mpi20.model.ThreeDSMethodData;
import dev.vality.proxy.mocketbank.utils.UrlUtils;
import dev.vality.proxy.mocketbank.utils.state.constant.SuspendPrefix;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/${server.rest.endpoint}")
public class MocketBankController {

    private static final String EMPTY_STRING = "";

    private final HellgateClient hellgateClient;
    private final AdapterMockBankProperties mockBankProperties;
    private final ObjectMapper objectMapper;

    @RequestMapping(value = "term_url", method = RequestMethod.POST)
    public String receiveIncomingParameters(
            HttpServletRequest request,
            HttpServletResponse servletResponse) throws IOException {
        String tag = getTag(request);
        log.info("ReceivePaymentIncomingParameters with tag {}, info {}", tag, httpServletRequestToString(request));
        String resp = EMPTY_STRING;
        try {
            ByteBuffer callback = prepareCallbackParams(request);
            ByteBuffer response = hellgateClient.processPaymentCallback(tag, callback);
            resp = new String(response.array(), StandardCharsets.UTF_8);
        } catch (HellgateException e) {
            log.warn("Failed handle callback for payment", e);
        } catch (Exception e) {
            log.error("Failed handle callback for payment", e);
        }
        sendRedirect(request, servletResponse);
        return resp;
    }

    @RequestMapping(value = "three_ds_method_notification", method = RequestMethod.POST)
    public String mpi20ThreeDsMethodNotification(HttpServletRequest servletRequest,
                                                 HttpServletResponse servletResponse) throws IOException {
        log.info("three_ds_method_notification {}", httpServletRequestToString(servletRequest));
        String threeDsMethodData = URLDecoder.decode(
                servletRequest.getParameter(CallbackResponseFields.THREE_DS_METHOD_DATA), StandardCharsets.UTF_8);
        ThreeDSMethodData threeDSMethodData = objectMapper.readValue(threeDsMethodData, ThreeDSMethodData.class);
        String tag = SuspendPrefix.PAYMENT.getPrefix() + threeDSMethodData.getThreeDSServerTransID();
        ByteBuffer callback = prepareCallbackParams(servletRequest);
        String response = EMPTY_STRING;
        try {
            ByteBuffer callbackResponse = hellgateClient.processPaymentCallback(tag, callback);
            response = new String(callbackResponse.array(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed handle mpi20 three_ds_method_notification", e);
        }
        return response;
    }

    @RequestMapping(value = "acs_notification", method = RequestMethod.POST)
    public String mpi20AcsNotification(HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) throws IOException {
        log.info("mpi20 acs_notification {}", httpServletRequestToString(servletRequest));
        CRes challengeRes = objectMapper.readValue(
                servletRequest.getParameter(CallbackResponseFields.CRES), CRes.class);
        String tag = SuspendPrefix.PAYMENT.getPrefix() + challengeRes.getThreeDSServerTransID();
        ByteBuffer callback = prepareCallbackParams(servletRequest);
        String response = EMPTY_STRING;
        try {
            ByteBuffer callbackResponse = hellgateClient.processPaymentCallback(tag, callback);
            response = new String(callbackResponse.array(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed handle mpi20 acs_notification", e);
        }
        sendRedirect(servletRequest, servletResponse);
        return response;
    }

    @RequestMapping(value = "/p2p", method = RequestMethod.POST)
    public String receiveP2pIncomingParameters(HttpServletRequest request,
                                               HttpServletResponse servletResponse) throws IOException {
        String tag = getTag(request);
        log.info("receiveP2pIncomingParameters with tag {}, info {}", tag, httpServletRequestToString(request));
        throw new UnsupportedOperationException("p2p is not supported");
    }

    @RequestMapping(value = "/qps", method = RequestMethod.GET)
    public String receiveQpsIncomingParameters(
            HttpServletRequest request,
            HttpServletResponse servletResponse) throws IOException {
        log.info("receiveQpsIncomingParameters with info {}", httpServletRequestToString(request));
        servletResponse.sendRedirect(mockBankProperties.getFinishInteraction());
        return EMPTY_STRING;
    }

    @RequestMapping(value = "/dw", method = RequestMethod.POST)
    public String receiveDwIncomingParameters(
            HttpServletRequest request,
            HttpServletResponse servletResponse) throws IOException {
        log.info("receiveDWIncomingParameters with info {}", httpServletRequestToString(request));
        servletResponse.sendRedirect(mockBankProperties.getFinishInteraction());
        return EMPTY_STRING;
    }

    private String getTag(HttpServletRequest request) {
        if (StringUtils.hasText(request.getParameter("MD"))) {
            return request.getParameter("MD");
        } else {
            log.warn("Missing a required parameter 'MD' ");
        }
        return EMPTY_STRING;
    }

    private ByteBuffer prepareCallbackParams(HttpServletRequest request) throws JsonProcessingException {
        Map<String, String> requestParams = CommonConverter.mapArrayToMap(request.getParameterMap());
        return CommonConverter.mapToByteBuffer(requestParams);
    }

    private void sendRedirect(HttpServletRequest request, HttpServletResponse servletResponse) throws IOException {
        if (StringUtils.hasText(request.getParameter(UrlUtils.TERMINATION_URI_REQUEST_PARAM_NAME))) {
            servletResponse.sendRedirect(request.getParameter(UrlUtils.TERMINATION_URI_REQUEST_PARAM_NAME));
        }
    }

    private String httpServletRequestToString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Request Method = [").append(request.getMethod()).append("], ");
        sb.append("Request URL Path = [").append(request.getRequestURL()).append("], ");

        String headers = Collections.list(request.getHeaderNames())
                .stream()
                .map(headerName -> headerName + " : " + Collections.list(request.getHeaders(headerName)))
                .collect(Collectors.joining(", "));

        if (headers.isEmpty()) {
            sb.append("Request headers: NONE,");
        } else {
            sb.append("Request headers: [").append(headers).append("],");
        }

        String parameters = Collections.list(request.getParameterNames())
                .stream()
                .map(p -> p + " : " + Arrays.asList(request.getParameterValues(p)))
                .collect(Collectors.joining(", "));

        if (parameters.isEmpty()) {
            sb.append("Request parameters: NONE.");
        } else {
            sb.append("Request parameters: [").append(parameters).append("].");
        }

        return sb.toString();
    }

}
