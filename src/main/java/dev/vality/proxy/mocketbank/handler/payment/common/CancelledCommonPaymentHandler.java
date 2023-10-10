package dev.vality.proxy.mocketbank.handler.payment.common;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.handler.payment.CommonPaymentHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createFinishIntentSuccess;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createPaymentProxyResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class CancelledCommonPaymentHandler implements CommonPaymentHandler {

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetCancelled();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        return createPaymentProxyResult(createFinishIntentSuccess(),
                PaymentState.CANCELLED.getBytes(), context.getPaymentInfo().getPayment().getTrx()
        );
    }

}
