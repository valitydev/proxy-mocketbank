package dev.vality.proxy.mocketbank.utils;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.BankCardTokenServiceRef;
import dev.vality.damsel.domain.DisposablePaymentResource;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.proxy_provider.InvoicePayment;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentInfo;
import dev.vality.damsel.proxy_provider.PaymentResource;

import java.util.Optional;

public class TokenProviderVerification {

    public static boolean hasBankCardTokenProvider(Object object) {
        String bankCardTokenProvider;
        bankCardTokenProvider = extractBankCardTokenProvider((PaymentContext) object);
        return bankCardTokenProvider != null;
    }


    public static String extractBankCardTokenProvider(PaymentContext context) {
        return Optional.ofNullable(context.getPaymentInfo())
                .map(PaymentInfo::getPayment)
                .map(InvoicePayment::getPaymentResource)
                .map(PaymentResource::getDisposablePaymentResource)
                .map(DisposablePaymentResource::getPaymentTool)
                .map(PaymentTool::getBankCard)
                .map(BankCard::getPaymentToken)
                .map(BankCardTokenServiceRef::getId)
                .orElse(null);
    }

}
