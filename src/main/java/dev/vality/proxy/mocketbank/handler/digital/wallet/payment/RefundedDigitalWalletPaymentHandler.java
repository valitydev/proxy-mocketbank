package dev.vality.proxy.mocketbank.handler.digital.wallet.payment;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.handler.digital.wallet.CommonDigitalWalletHandler;
import dev.vality.proxy.mocketbank.validator.DigitalWalletRefundedValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createFinishIntentSuccess;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createPaymentProxyResult;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundedDigitalWalletPaymentHandler implements CommonDigitalWalletHandler {

    private final DigitalWalletRefundedValidator digitalWalletRefundedValidator;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetRefunded();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        digitalWalletRefundedValidator.validate(context, context.getOptions());
        return createPaymentProxyResult(createFinishIntentSuccess(),
                PaymentState.REFUNDED.getBytes(), context.getPaymentInfo().getRefund().getTrx()
        );
    }

}
