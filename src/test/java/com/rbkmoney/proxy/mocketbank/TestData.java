package com.rbkmoney.proxy.mocketbank;

import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.java.cds.utils.creators.CdsPackageCreators;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;
import com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestData {
    public static final String PHONE_NUMBER = "9876543210";
    public static final String WITHDRAWAL_TOKEN = "token";
    public static final String BANK_CARD_TOKEN = "bank_card_token";
    public static final String RECURRENT_TOKEN = "recurrent_token";
    public static final String DEFAULT_CARDHOLDERNAME = "NONAME";

    public static final String FINGERPRINT = "fingerprint";
    public static final String IP_ADDRESS = "0.0.0.0";
    public static final String SESSION_ID = "session_id";
    public static final String CREATED_AT = "2020-06-02";

    public static final String DEFAULT_ACS_URL = "http://localhost/acs";
    public static final String DEFAULT_MPIV2_PREPARE_URL = "http://localhost/mpi20/three_ds_method";
    public static final String DEFAULT_MPIV2_ACS_URL = "http://localhost/mpi20/acs";
    public static final String DEFAULT_PAREQ = "PaReq";

    public static final String DEFAULT_YEAR = "2020";
    public static final String DEFAULT_MONTH = "12";
    public static final String DEFAULT_CVV = "123";
    public static final String DEFAULT_CARD = "4012001011000771";
    public static final String DEFAULT_BIN = "123456";

    public static final String DEFAULT_THREE_DS_TRANS_ID = "testThreeDSServerTransID";
    public static final String DEFAULT_THREE_METHOD_DATA =
            "{\"threeDSServerTransID\":\"3-D Secure 2.0 Success with Get Acs64b16722-78cf-41c9-b401-e13f798128a8\"}\"";


    public static CardDataProxyModel createCardDataProxyModel(String pan) {
        return CardDataProxyModel.builder()
                .pan(pan)
                .expMonth(Byte.parseByte(DEFAULT_MONTH))
                .expYear(Short.parseShort(DEFAULT_YEAR))
                .cardholderName(DEFAULT_CARDHOLDERNAME)
                .build();
    }

    public static CardData createCardData(String pan) {
        return CdsPackageCreators.createCardData(pan);
    }

    public static CardData createCardData() {
        return CdsPackageCreators.createCardData(DEFAULT_CARD);
    }

    public static BankCard createBankCard(CardData cardData) {

        return DomainPackageCreators.createBankCard(
                TestData.DEFAULT_MONTH,
                TestData.DEFAULT_YEAR,
                TestData.DEFAULT_CARDHOLDERNAME)
                .setPaymentSystem(new PaymentSystemRef())
                .setBin(DEFAULT_BIN)
                .setLastDigits(cardData.pan.substring(cardData.pan.length() - 4));
    }

}
