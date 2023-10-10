package dev.vality.proxy.mocketbank.handler.payment.recurrent;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.Intent;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.handler.payment.CommonPaymentHandler;
import dev.vality.proxy.mocketbank.utils.CreatorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractInvoiceId;
import static dev.vality.adapter.common.damsel.ProxyProviderVerification.isMakeRecurrent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedRecurrentCommonPaymentHandler implements CommonPaymentHandler {

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetProcessed() && paymentResource.isSetRecurrentPaymentResource();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        Intent intent = createFinishIntentSuccess();
        if (isMakeRecurrent(context)) {
            String invoiceId = extractInvoiceId(context);
            intent = createFinishIntentSuccessWithToken(invoiceId);
        }

        TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);
        return createPaymentProxyResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
    }
}
