package dev.vality.proxy.mocketbank.service.oct.verification;

import dev.vality.damsel.withdrawals.provider_adapter.GetQuoteParams;
import dev.vality.proxy.mocketbank.service.oct.constant.CryptoCurrencies;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyVerification {

    public static boolean isCryptoCurrency(dev.vality.damsel.domain.Currency currency) {
        return Arrays.asList(CryptoCurrencies.toArraySymbol()).contains(currency.getSymbolicCode());
    }

    public static boolean isCurrencyEquals(
            dev.vality.damsel.domain.Currency currencyFrom,
            dev.vality.damsel.domain.Currency currencyTo) {
        return currencyFrom.getSymbolicCode().equalsIgnoreCase(currencyTo.getSymbolicCode())
                || (currencyFrom.getNumericCode() == currencyTo.getNumericCode());
    }

    public static boolean isExchangeCurrencyEqualsOtherCurrency(GetQuoteParams getQuoteParams) {
        dev.vality.damsel.domain.Currency currencyFrom = getQuoteParams.getCurrencyFrom();
        dev.vality.damsel.domain.Currency currencyTo = getQuoteParams.getCurrencyTo();
        dev.vality.damsel.domain.Currency exchangeCurrency = getQuoteParams.getExchangeCash().getCurrency();
        return isCurrencyEquals(currencyFrom, exchangeCurrency) || isCurrencyEquals(currencyTo, exchangeCurrency);
    }

}
