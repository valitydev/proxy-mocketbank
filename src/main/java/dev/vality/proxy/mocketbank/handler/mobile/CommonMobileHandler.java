package dev.vality.proxy.mocketbank.handler.mobile;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import org.apache.thrift.TException;

public interface CommonMobileHandler {

    boolean filter(final TargetInvoicePaymentStatus targetInvoicePaymentStatus, final PaymentResource paymentResource);

    PaymentProxyResult handler(PaymentContext context) throws TException;

}
