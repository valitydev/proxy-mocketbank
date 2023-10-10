package dev.vality.proxy.mocketbank.handler.terminal;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import org.apache.thrift.TException;

public interface CommonTerminalHandler {

    boolean filter(final TargetInvoicePaymentStatus targetInvoicePaymentStatus, final PaymentResource paymentResource);

    PaymentProxyResult handler(PaymentContext context) throws TException;

}
