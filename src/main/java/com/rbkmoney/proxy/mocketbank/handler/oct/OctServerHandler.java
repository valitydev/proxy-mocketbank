package com.rbkmoney.proxy.mocketbank.handler.oct;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import com.rbkmoney.cds.client.storage.exception.CdsStorageException;
import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.cds.storage.StorageSrv;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.TransactionInfo;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.domain.Destination;
import com.rbkmoney.damsel.withdrawals.provider_adapter.*;
import com.rbkmoney.error.mapping.ErrorMapping;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators;
import com.rbkmoney.java.damsel.utils.creators.WithdrawalsProviderAdapterPackageCreators;
import com.rbkmoney.proxy.mocketbank.service.oct.verification.CurrencyVerification;
import com.rbkmoney.proxy.mocketbank.utils.ErrorBuilder;
import com.rbkmoney.proxy.mocketbank.utils.payout.CardPayout;
import com.rbkmoney.proxy.mocketbank.utils.payout.CardPayoutAction;
import com.rbkmoney.proxy.mocketbank.utils.payout.PayoutUtils;
import com.rbkmoney.proxy.mocketbank.validator.WithdrawalValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OctServerHandler implements AdapterSrv.Iface {

    private static final Name FAKER_NAME = new Faker(Locale.ENGLISH).name();
    private static final String NAME_REGEXP = "[^a-zA-Z +]";

    private final StorageSrv.Iface storageSrv;
    private final ErrorMapping errorMapping;
    private final List<CardPayout> cardPayoutList;
    private final WithdrawalValidator withdrawalValidator;

    @Override
    public ProcessResult processWithdrawal(
            Withdrawal withdrawal,
            Value state,
            Map<String, String> options) {

        if (withdrawal.getDestination().isSetBankCard()) {
            CardDataProxyModel cardData = getCardDataForWithdrawal(withdrawal);
            log.info("cardPayoutList {}", cardPayoutList);
            Optional<CardPayout> cardPayout = PayoutUtils.extractCardPayoutByPan(cardPayoutList, cardData.getPan());
            if (cardPayout.isPresent()) {
                log.info("Found card payout with action {}", cardPayout.get().getAction());
                if (CardPayoutAction.isCardFailed(cardPayout.get())) {
                    CardPayoutAction action = CardPayoutAction.findByValue(cardPayout.get().getAction());
                    log.info("Failed card payout with action {}", action);
                    return ErrorBuilder.prepareWithdrawalError(errorMapping, action);
                }
            }
        }

        TransactionInfo transactionInfo =
                DomainPackageCreators.createTransactionInfo(withdrawal.getId(), Collections.emptyMap());
        Intent intent = WithdrawalsProviderAdapterPackageCreators.createFinishIntentSuccess(transactionInfo);
        return WithdrawalsProviderAdapterPackageCreators.createProcessResult(intent);
    }

    private CardDataProxyModel getCardDataForWithdrawal(Withdrawal withdrawal) {
        Destination destination = withdrawal.getDestination();
        if (!destination.isSetBankCard()) {
            throw new CdsStorageException("Token must be set for card data, withdrawalId " + withdrawal.getId());
        }
        BankCard bankCard = destination.getBankCard();
        CardData cardData = getCardData(bankCard.getToken());
        return initCardDataProxyModel(bankCard, cardData);
    }

    private CardDataProxyModel initCardDataProxyModel(BankCard bankCard, CardData cardData) {
        String cardHolder = extractCardHolder(bankCard, cardData);
        return CardDataProxyModel.builder()
                .cardholderName(cardHolder)
                .pan(cardData.getPan())
                .expMonth(getExpMonth(bankCard, cardData))
                .expYear(getExpYear(bankCard, cardData))
                .build();
    }

    private static String extractCardHolder(BankCard bankCard, CardData cardData) {
        if (bankCard.isSetCardholderName()) {
            return bankCard.getCardholderName();
        } else if (cardData.isSetCardholderName()) {
            return cardData.getCardholderName();
        } else {
            return (FAKER_NAME.firstName() + StringUtils.SPACE + FAKER_NAME.lastName())
                    .replaceAll(NAME_REGEXP, StringUtils.EMPTY)
                    .toUpperCase();
        }
    }

    private static byte getExpMonth(BankCard bankCard, CardData cardData) {
        if (bankCard.isSetExpDate()) {
            return bankCard.getExpDate().getMonth();
        }
        return cardData.isSetExpDate() ? cardData.getExpDate().getMonth() : 0;
    }

    private static short getExpYear(BankCard bankCard, CardData cardData) {
        if (bankCard.isSetExpDate()) {
            return bankCard.getExpDate().getYear();
        }
        return cardData.isSetExpDate() ? cardData.getExpDate().getYear() : 0;
    }


    private CardData getCardData(String token) {
        log.info("Get card data by token: {}", token);
        try {
            return storageSrv.getCardData(token);
        } catch (TException ex) {
            throw new CdsStorageException(String.format("Can't get card data with token: %s", token), ex);
        }
    }

    @Override
    public Quote getQuote(GetQuoteParams getQuoteParams, Map<String, String> options) {
        withdrawalValidator.validate(getQuoteParams, options);

        Cash cashFrom = new Cash().setCurrency(getQuoteParams.getCurrencyFrom());
        Cash cashTo = new Cash().setCurrency(getQuoteParams.getCurrencyTo());
        if (CurrencyVerification.isCryptoCurrency(getQuoteParams.getCurrencyTo())) {
            cashFrom.setAmount(getQuoteParams.getExchangeCash().getAmount());
            cashTo.setAmount(getQuoteParams.getExchangeCash().getAmount() * 2);
        } else {
            cashFrom.setAmount(getQuoteParams.getExchangeCash().getAmount() * 2);
            cashTo.setAmount(getQuoteParams.getExchangeCash().getAmount());
        }

        Value quoteData = new Value();
        quoteData.setStr(getQuoteParams.getIdempotencyId());
        return new Quote()
                .setCashTo(cashTo)
                .setCashFrom(cashFrom)
                .setQuoteData(quoteData)
                .setCreatedAt(getCurrentDateTimeByPattern(Instant.EPOCH.toEpochMilli()))
                .setExpiresOn(getCurrentDateTimeByPattern(Instant.EPOCH
                        .plus(15, ChronoUnit.MINUTES)
                        .toEpochMilli())
                );
    }

    @Override
    public CallbackResult handleCallback(
            Callback callback,
            Withdrawal withdrawal,
            Value value,
            Map<String, String> map) throws TException {
        throw new TException("Not supported method handleCallback");
    }

    private static String getCurrentDateTimeByPattern(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).toString();
    }

}
