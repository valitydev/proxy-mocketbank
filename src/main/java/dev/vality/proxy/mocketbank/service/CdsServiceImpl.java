package dev.vality.proxy.mocketbank.service;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.common.exception.CdsStorageException;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.withdrawals.domain.Destination;
import dev.vality.damsel.withdrawals.provider_adapter.Withdrawal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import net.datafaker.providers.base.Name;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class CdsServiceImpl implements CdsService {

    private static final Name FAKER_NAME = new Faker(Locale.ENGLISH).name();
    private static final String NAME_REGEXP = "[^a-zA-Z +]";

    private final CdsStorageClient cds;

    @Override
    public CardDataProxyModel getCardData(Withdrawal withdrawal) {
        Destination destination = withdrawal.getDestination();
        if (!destination.isSetBankCard()) {
            throw new CdsStorageException("Token must be set for card data, withdrawalId " + withdrawal.getId());
        }
        BankCard bankCard = destination.getBankCard();
        CardData cardData = cds.getCardData(bankCard.getToken());
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
}
