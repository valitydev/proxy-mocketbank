package dev.vality.proxy.mocketbank.decorator;

import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.slf4j.MDC;

import java.nio.ByteBuffer;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractInvoiceId;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractPaymentId;

@RequiredArgsConstructor
public class PaymentServerHandlerMdcLog implements ProviderProxySrv.Iface {

    private final ProviderProxySrv.Iface handler;

    private void mdcPut(PaymentContext context) {
        String invoiceId = extractInvoiceId(context);
        String paymentId = extractPaymentId(context);
        MDC.put("invoiceId", invoiceId);
        MDC.put("paymentId", paymentId);
    }

    private void mdcRemove() {
        MDC.remove("invoiceId");
        MDC.remove("paymentId");
    }

    @Override
    public PaymentProxyResult processPayment(PaymentContext context) throws TException {
        mdcPut(context);
        try {
            return handler.processPayment(context);
        } finally {
            mdcRemove();
        }
    }

    @Override
    public PaymentCallbackResult handlePaymentCallback(ByteBuffer callback, PaymentContext context) throws TException {
        mdcPut(context);
        try {
            return handler.handlePaymentCallback(callback, context);
        } finally {
            mdcRemove();
        }
    }
}
