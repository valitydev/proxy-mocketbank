package dev.vality.proxy.mocketbank.handler.mobile.payment;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.handler.mobile.CommonMobileHandler;
import org.apache.thrift.TException;

public class UnsupportedMobileHandler implements CommonMobileHandler {

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
