package dev.vality.proxy.mocketbank.handler.digital.wallet.payment;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.Intent;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.handler.digital.wallet.CommonDigitalWalletHandler;
import dev.vality.proxy.mocketbank.validator.DigitalWalletCapturedValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CapturedDigitalWalletPaymentHandler implements CommonDigitalWalletHandler {

    private final DigitalWalletCapturedValidator digitalWalletCapturedValidator;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetCaptured();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        digitalWalletCapturedValidator.validate(context, context.getOptions());
        Intent intent = createFinishIntentSuccess();
        TransactionInfo transactionInfo = extractTransactionInfo(context);
        return createPaymentProxyResult(intent, extractSessionState(context), transactionInfo);
    }

}
