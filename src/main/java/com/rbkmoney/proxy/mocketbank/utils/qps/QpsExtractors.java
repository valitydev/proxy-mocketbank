package com.rbkmoney.proxy.mocketbank.utils.qps;

import com.rbkmoney.damsel.domain.PaymentServiceRef;
import com.rbkmoney.damsel.domain.PaymentTool;
import com.rbkmoney.damsel.proxy_provider.PaymentContext;
import com.rbkmoney.damsel.proxy_provider.PaymentResource;
import com.rbkmoney.java.damsel.utils.extractors.ProxyProviderPackageExtractors;
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
