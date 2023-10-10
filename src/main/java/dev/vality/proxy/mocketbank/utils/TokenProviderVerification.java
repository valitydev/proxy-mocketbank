package dev.vality.proxy.mocketbank.utils;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.BankCardTokenServiceRef;
import dev.vality.damsel.domain.DisposablePaymentResource;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.proxy_provider.*;

import java.util.Optional;

public class TokenProviderVerification {

    public static boolean hasBankCardTokenProvider(Object object) {
        String bankCardTokenProvider;
        if (object instanceof RecurrentTokenContext) {
            bankCardTokenProvider = extractBankCardTokenProvider((RecurrentTokenContext) object);
        } else {
            bankCardTokenProvider = extractBankCardTokenProvider((PaymentContext) object);
        }

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

    public static String extractBankCardTokenProvider(RecurrentTokenContext context) {
        return Optional.ofNullable(context.getTokenInfo())
                .map(RecurrentTokenInfo::getPaymentTool)
                .map(RecurrentPaymentTool::getPaymentResource)
                .map(DisposablePaymentResource::getPaymentTool)
                .map(PaymentTool::getBankCard)
                .map(BankCard::getPaymentToken)
                .map(BankCardTokenServiceRef::getId)
                .orElse(null);
    }
}
