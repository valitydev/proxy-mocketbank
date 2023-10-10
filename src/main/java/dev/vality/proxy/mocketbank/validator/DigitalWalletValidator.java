package dev.vality.proxy.mocketbank.validator;

import dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.exception.DigitalWalletException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DigitalWalletValidator implements Validator<PaymentContext> {

    public void validate(PaymentContext context, Map<String, String> options) {
        PaymentResource paymentResource =
                dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractPaymentResource(
                        context);
        PaymentTool paymentTool = ProxyProviderPackageExtractors.extractPaymentTool(paymentResource);
        if (!paymentTool.isSetDigitalWallet()) {
            throw new DigitalWalletException("Isn`t payment tool payment terminal");
        }
    }
}

