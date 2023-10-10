package dev.vality.proxy.mocketbank.validator;

import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.exception.PaymentException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractPaymentResource;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractPaymentTool;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentValidator implements Validator<PaymentContext> {

    public void validate(PaymentContext context, Map<String, String> options) {
        PaymentResource paymentResource = extractPaymentResource(context);
        PaymentTool paymentTool = extractPaymentTool(paymentResource);
        if (!paymentResource.isSetRecurrentPaymentResource() && !paymentTool.isSetBankCard()) {
            throw new PaymentException("Isn`t payment tool bank card");
        }
    }

}

