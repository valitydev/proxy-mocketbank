package dev.vality.proxy.mocketbank.handler.terminal.payment;

import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.*;
import dev.vality.damsel.user_interaction.UserInteraction;
import dev.vality.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import dev.vality.proxy.mocketbank.configuration.properties.TimerProperties;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.handler.terminal.CommonTerminalHandler;
import dev.vality.proxy.mocketbank.handler.terminal.TerminalConstant;
import dev.vality.proxy.mocketbank.utils.CreatorUtils;
import dev.vality.proxy.mocketbank.utils.UrlUtils;
import dev.vality.proxy.mocketbank.utils.qps.QpsCreators;
import dev.vality.proxy.mocketbank.utils.qps.QpsExtractors;
import dev.vality.proxy.mocketbank.utils.qps.QpsOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractInvoiceId;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedTerminalCommonHandler implements CommonTerminalHandler {

    private final TimerProperties timerProperties;
    private final AdapterMockBankProperties mockBankProperties;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetProcessed();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        String invoiceId = extractInvoiceId(context);
        Intent intent = createFinishIntentSuccess();
        TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);

        if (TerminalConstant.QPS.equals(QpsExtractors.extractTerminalPaymentProvider(context))) {
            return qpsInteraction(context, intent, invoiceId, transactionInfo);
        }

        return createPaymentProxyResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
    }

    private PaymentProxyResult qpsInteraction(
            PaymentContext context,
            Intent intent,
            String invoiceId,
            TransactionInfo transactionInfo) {
        if (Arrays.equals(context.getSession().getState(), PaymentState.SLEEP.getBytes())) {
            return createPaymentProxyResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
        }

        Cash cash = context.getPaymentInfo().getPayment().getCost();
        MultiValueMap<String, String> params = QpsCreators.createQpsParams(invoiceId, cash);
        String payload = UrlUtils.getCallbackUrl(
                mockBankProperties.getCallbackUrl(),
                mockBankProperties.getPathQpsCallbackUrl(),
                params);
        intent = createIntentWithSleepIntent(
                QpsOptions.extractQpsTimerTimeout(context.getOptions(), timerProperties.getQpsTimeout()),
                UserInteraction.qr_code_display_request(QpsCreators.createQrCodeDisplayRequest(payload)));
        return createPaymentProxyResult(intent, PaymentState.SLEEP.getBytes(), transactionInfo);
    }

}
