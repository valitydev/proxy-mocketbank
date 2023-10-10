package dev.vality.proxy.mocketbank.handler.digital.wallet.payment;

import dev.vality.damsel.domain.DigitalWallet;
import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.Cash;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import dev.vality.proxy.mocketbank.configuration.properties.TimerProperties;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.handler.digital.wallet.CommonDigitalWalletHandler;
import dev.vality.proxy.mocketbank.utils.CreatorUtils;
import dev.vality.proxy.mocketbank.utils.UrlUtils;
import dev.vality.proxy.mocketbank.utils.dw.DwCreators;
import dev.vality.proxy.mocketbank.utils.dw.DwExtractors;
import dev.vality.proxy.mocketbank.utils.dw.DwOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractCashPayment;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractInvoiceId;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedDigitalWalletPaymentHandler implements CommonDigitalWalletHandler {

    private final TimerProperties timerProperties;
    private final AdapterMockBankProperties mockBankProperties;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetProcessed() && paymentResource.isSetDisposablePaymentResource();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) {
        String invoiceId = extractInvoiceId(context);
        TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);

        if (Arrays.equals(context.getSession().getState(), PaymentState.SLEEP.getBytes())) {
            return createPaymentProxyResult(
                    createFinishIntentSuccess(),
                    PaymentState.CAPTURED.getBytes(),
                    transactionInfo);
        }

        Cash cash = extractCashPayment(context);
        DigitalWallet digitalWallet = DwExtractors.extractDigitalWallet(context);
        HashMap<String, String> params = DwCreators.createDigitalWalletParams(
                invoiceId, cash, digitalWallet, mockBankProperties);
        String url = UrlUtils.getCallbackUrl(
                mockBankProperties.getCallbackUrl(),
                mockBankProperties.getPathDigitalWalletCallbackUrl());

        return createPaymentProxyResult(
                createIntentWithSleepIntent(
                        DwOptions.extractDigitalWalletTimerTimeout(
                                context.getOptions(), timerProperties.getDwTimeout()),
                        createPostUserInteraction(url, params)
                ),
                PaymentState.SLEEP.getBytes(), transactionInfo);
    }
}
