package dev.vality.proxy.mocketbank.validator;

import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.proxy.mocketbank.exception.PaymentException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DigitalWalletCapturedValidator implements Validator<PaymentContext> {

    public void validate(PaymentContext context, Map<String, String> options) {
        if (!context.getPaymentInfo().isSetCapture()) {
            throw new PaymentException("Captured not set");
        }
    }

}

