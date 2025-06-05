package dev.vality.proxy.mocketbank.handler.oct;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.identity_document_storage.IdentityDocument;
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

    public static String WITHDRAWALID = "TEST_WITHDRAWAL_ID";
    public static final String SENDER = "sender";


    protected Withdrawal createWithdrawal(BankCard bankCard) {
        Destination destination = createDestination(bankCard);

        return new Withdrawal()
                .setId(WITHDRAWALID)
                .setDestination(destination)
                .setBody(createCash())
                .setSender(SENDER);
    }


    protected Destination createDestination(BankCard bankCard) {
        Destination destination = new Destination();
        destination.setBankCard(bankCard);
        return destination;
    }

    protected Cash createCash() {
        return new Cash()
                .setAmount(1000L)
                .setCurrency(createCurrency("Rubles", (short) 643, "RUB", (short) 2));
    }

    protected Map<String, String> createProxyOptions() {
        return Collections.emptyMap();
    }

    protected String randomString() {
        return UUID.randomUUID().toString();
    }

}
