package dev.vality.proxy.mocketbank.utils.dw;

import dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors;
import dev.vality.damsel.domain.DigitalWallet;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DwExtractors {

    public static DigitalWallet extractDigitalWallet(PaymentContext context) {
        PaymentResource paymentResource = ProxyProviderPackageExtractors.extractPaymentResource(context);
        if (paymentResource.isSetDisposablePaymentResource()) {
            PaymentTool paymentTool = paymentResource.getDisposablePaymentResource().getPaymentTool();
            if (paymentTool.isSetDigitalWallet()) {
                return paymentTool.getDigitalWallet();
            }
        }
        return null;
    }
}
