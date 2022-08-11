package com.rbkmoney.proxy.mocketbank.service;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import com.rbkmoney.cds.client.storage.exception.CdsStorageException;
import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.cds.storage.StorageSrv;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.withdrawals.domain.Destination;
import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class CdsServiceImpl implements CdsService {

    private static final Name FAKER_NAME = new Faker(Locale.ENGLISH).name();
    private static final String NAME_REGEXP = "[^a-zA-Z +]";

    private final StorageSrv.Iface storageSrv;

    @Override
    public CardDataProxyModel getCardData(Withdrawal withdrawal) {
        Destination destination = withdrawal.getDestination();
        if (!destination.isSetBankCard()) {
            throw new CdsStorageException("Token must be set for card data, withdrawalId " + withdrawal.getId());
        }
        BankCard bankCard = destination.getBankCard();
        CardData cardData = callCds(bankCard.getToken());
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


    private CardData callCds(String token) {
        log.info("Get card data by token: {}", token);
        try {
            return storageSrv.getCardData(token);
        } catch (TException ex) {
            throw new CdsStorageException(String.format("Can't get card data with token: %s", token), ex);
        }
    }
}
