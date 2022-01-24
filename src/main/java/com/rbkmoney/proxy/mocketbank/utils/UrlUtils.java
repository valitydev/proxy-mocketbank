package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.damsel.proxy_provider.InvoicePayment;
import com.rbkmoney.proxy.mocketbank.service.mpi.constant.MpiField;
import com.rbkmoney.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UrlUtils {

    public static final String TERMINATION_URI_REQUEST_PARAM_NAME = "termination_uri";

    public static String getCallbackUrl(String callbackUrl, String path) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
                .path(path)
                .build()
                .toUriString();
    }

    public static String getCallbackUrl(String callbackUrl, String path, String paramName, String paramValue) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
                .path(path)
                .queryParam(paramName, paramValue)
                .build()
                .toUriString();
    }

    public static String getCallbackUrl(String url, Map<String, String> params) {
        MultiValueMap<String, String> multiValueMapParams = new LinkedMultiValueMap<>();
        multiValueMapParams.setAll(params);
        return getCallbackUrl(url, null, multiValueMapParams);
    }

    public static String getCallbackUrl(String callbackUrl, String path, MultiValueMap<String, String> params) {
        return UriComponentsBuilder.fromUriString(callbackUrl)
                .path(path)
                .queryParams(params)
                .build()
                .toUriString();
    }

    public static Map<String, String> prepareRedirectParams(
            VerifyEnrollmentResponse verifyEnrollmentResponse, String tag, String termUrl
    ) {
        Map<String, String> params = new HashMap<>();
        params.put(MpiField.PA_REQ.getValue(), verifyEnrollmentResponse.getPaReq());
        params.put(MpiField.MD.getValue(), tag);
        params.put(MpiField.TERM_URL.getValue(), URLEncoder.encode(termUrl, StandardCharsets.UTF_8));
        return params;
    }

    public static MultiValueMap<String, String> getTerminationUrlAsParam(InvoicePayment payment, String defaultValue) {
        MultiValueMap<String, String> param = new LinkedMultiValueMap<>();
        param.add(
                TERMINATION_URI_REQUEST_PARAM_NAME,
                hasRedirectUrl(payment) ? payment.getPayerSessionInfo().getRedirectUrl() : defaultValue);
        return param;
    }

    public static boolean hasRedirectUrl(InvoicePayment payment) {
        return payment != null && payment.isSetPayerSessionInfo()
                && StringUtils.hasText(payment.getPayerSessionInfo().getRedirectUrl());
    }

}
