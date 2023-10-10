package dev.vality.proxy.mocketbank.handler.payment.common;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.*;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.handler.payment.CommonPaymentHandler;
import dev.vality.proxy.mocketbank.validator.PaymentCapturedValidator;
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
public class CapturedCommonPaymentHandler implements CommonPaymentHandler {

    private final PaymentCapturedValidator paymentCapturedValidator;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetCaptured();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        paymentCapturedValidator.validate(context, context.getOptions());
        Intent intent = createFinishIntentSuccess();
        if (isMakeRecurrent(context)) {
            String invoiceId = extractInvoiceId(context);
            intent = createFinishIntentSuccessWithToken(invoiceId);
        }
        InvoicePayment payment = context.getPaymentInfo().getPayment();
        return createPaymentProxyResult(intent, PaymentState.CONFIRM.getBytes(), payment.getTrx());
    }

}
