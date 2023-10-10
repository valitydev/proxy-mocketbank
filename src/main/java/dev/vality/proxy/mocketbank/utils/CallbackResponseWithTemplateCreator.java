package dev.vality.proxy.mocketbank.utils;

import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.proxy.mocketbank.configuration.properties.Mpi20Properties;
import dev.vality.proxy.mocketbank.exception.RedirectTemplateException;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static dev.vality.proxy.mocketbank.service.mpi20.constant.CallbackResponseFields.*;
import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;

@Component
@RequiredArgsConstructor
public class CallbackResponseWithTemplateCreator {

    private final Mpi20Properties mpi20Properties;
    private final Configuration freemarkerConfiguration;

    public static final String TEMPLATE = "redirect.ftl";
    public static final String METHOD = "formMethod";


    public byte[] createCallbackResponseWithForm(
            Map<String, String> options,
            String formMethod,
            PaymentContext context) {
        String terminationUri = UrlUtils.hasRedirectUrl(context.getPaymentInfo().getPayment())
                ? context.getPaymentInfo().getPayment().getPayerSessionInfo().getRedirectUrl()
                : mpi20Properties.getReturnUrl();
        String acsTermUrl = UrlUtils.getCallbackUrl(
                mpi20Properties.getCallbackUrl(),
                mpi20Properties.getAcsNotificationPath(),
                TERMINATION_URI,
                terminationUri);
        String acsUrl = options.get(TERM_URL);
        String creq = StringEscapeUtils.escapeHtml4(options.get(CREQ));
        try {
            return processTemplateIntoString(freemarkerConfiguration.getTemplate(TEMPLATE), Map.of(
                    ACS_URL, acsUrl,
                    METHOD, formMethod,
                    CREQ, creq,
                    TERM_URL, acsTermUrl
            )).getBytes();
        } catch (IOException | TemplateException e) {
            throw new RedirectTemplateException("Failed process redirect template into string", e);
        }
    }

}
