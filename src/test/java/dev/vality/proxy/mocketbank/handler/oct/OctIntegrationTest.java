package dev.vality.proxy.mocketbank.handler.oct;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.withdrawals.domain.*;
import dev.vality.damsel.withdrawals.provider_adapter.Cash;
import dev.vality.damsel.withdrawals.provider_adapter.Withdrawal;
import dev.vality.proxy.mocketbank.TestData;
import dev.vality.proxy.mocketbank.decorator.WithdrawalServerHandlerLog;
import dev.vality.proxy.mocketbank.utils.payout.CardPayout;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static dev.vality.adapter.common.damsel.DomainPackageCreators.createCurrency;

public abstract class OctIntegrationTest {

    @Autowired
    protected WithdrawalServerHandlerLog handler;

    @Autowired
    protected List<CardPayout> cardPayoutList;

    protected static String WITHDRAWALID = "TEST_WITHDRAWAL_ID";


    protected Withdrawal createWithdrawal(BankCard bankCard) {
        Destination destination = createDestination(bankCard);

        List<IdentityDocument> identityDocumentList = createIdentityDocumentsList(TestData.WITHDRAWAL_TOKEN);
        Identity identity = createIdentity(identityDocumentList);
        return new Withdrawal()
                .setId(WITHDRAWALID)
                .setDestination(destination)
                .setBody(createCash())
                .setSender(identity);
    }

    protected List<IdentityDocument> createIdentityDocumentsList(String token) {
        List<IdentityDocument> identityDocumentList = new ArrayList<>();
        identityDocumentList.add(createIdentityDocument(token));
        return identityDocumentList;
    }

    protected Destination createDestination(BankCard bankCard) {
        Destination destination = new Destination();
        destination.setBankCard(bankCard);
        return destination;
    }

    protected Identity createIdentity(List<IdentityDocument> identityDocumentList) {
        return new Identity()
                .setContact(createContactDetailsList())
                .setDocuments(identityDocumentList);
    }

    protected IdentityDocument createIdentityDocument(String token) {
        return IdentityDocument.rus_domestic_passport(new RUSDomesticPassport().setToken(token));
    }

    protected Cash createCash() {
        return new Cash()
                .setAmount(1000L)
                .setCurrency(createCurrency("Rubles", (short) 643, "RUB", (short) 2));
    }

    protected Map<String, String> createProxyOptions() {
        return Collections.emptyMap();
    }

    protected List<ContactDetail> createContactDetailsList() {
        List<ContactDetail> contactDetailList = new ArrayList<>();
        ContactDetail contactDetail = new ContactDetail();
        contactDetail.setPhoneNumber(TestData.PHONE_NUMBER);
        contactDetailList.add(contactDetail);
        return contactDetailList;
    }

    protected String randomString() {
        return UUID.randomUUID().toString();
    }

}
