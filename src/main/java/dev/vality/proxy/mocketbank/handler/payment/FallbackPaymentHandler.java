package dev.vality.proxy.mocketbank.handler.payment;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import org.apache.thrift.TException;

public class FallbackPaymentHandler implements CommonPaymentHandler {

    @Override
    public boolean filter(
            final TargetInvoicePaymentStatus targetInvoicePaymentStatus,
            final PaymentResource paymentResource) {
        return false;
    }

    @Override
    public PaymentProxyResult handler(final PaymentContext context) throws TException {
        throw new TException("Unsupported method");
    }

}
