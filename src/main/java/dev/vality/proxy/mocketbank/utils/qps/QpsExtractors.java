package dev.vality.proxy.mocketbank.utils.qps;

import dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors;
import dev.vality.damsel.domain.PaymentServiceRef;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentResource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class QpsExtractors {

    public static String extractTerminalPaymentProvider(PaymentContext context) {
        PaymentResource paymentResource = ProxyProviderPackageExtractors.extractPaymentResource(context);
        if (paymentResource.isSetDisposablePaymentResource()) {
            PaymentTool paymentTool = paymentResource.getDisposablePaymentResource().getPaymentTool();
            if (paymentTool.isSetPaymentTerminal()) {
                return Optional.ofNullable(paymentTool.getPaymentTerminal().getPaymentService())
                        .map(PaymentServiceRef::getId).orElse(null);
            }
        } else if (paymentResource.isSetRecurrentPaymentResource()) {
            PaymentTool paymentTool = paymentResource.getRecurrentPaymentResource().getPaymentTool();
            if (paymentTool.isSetPaymentTerminal()) {
                return Optional.ofNullable(paymentTool.getPaymentTerminal().getPaymentService())
                        .map(PaymentServiceRef::getId).orElse(null);
            }
        }
        return null;
    }
}
