package dev.vality.proxy.mocketbank.handler.mobile.payment;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.Intent;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.handler.mobile.CommonMobileHandler;
import lombok.RequiredArgsConstructor;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;

@Component
@RequiredArgsConstructor
public class CapturedMobileCommonHandler implements CommonMobileHandler {

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetCaptured();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        Intent intent = createFinishIntentSuccess();
        TransactionInfo transactionInfo = extractTransactionInfo(context);
        return createPaymentProxyResult(intent, extractSessionState(context), transactionInfo);
    }

}
