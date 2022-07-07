package com.rbkmoney.proxy.mocketbank.handler.balance;

import dev.vality.scrooge.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class AccountBalanceHandler implements AccountServiceSrv.Iface {

    public static final String WRONG_ARGUMENT_EXCEPTION_MESSAGE = "Empty options in account balance request";
    private static final String MOCK_ACCOUNT_ID_ONE = "100192381092";
    private static final String MOCK_ACCOUNT_ID_TWO = "203817234980";

    @Override
    public BalanceResponse getBalance(BalanceRequest balanceRequest) {
        if (CollectionUtils.isEmpty(balanceRequest.getOptions())) {
            throw new IllegalArgumentException(WRONG_ARGUMENT_EXCEPTION_MESSAGE);
        }
        String requestTime = balanceRequest.isSetRequestTime()
                ? balanceRequest.getRequestTime()
                : ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT);
        log.info("Receive balance request dated {} with options: {}", requestTime, balanceRequest.getOptions());
        BalanceResponse response = buildResponse();
        Balance balance = response.getBalance();
        log.info("Response for account {} with balance {} {}",
                response.getAccountReference().getId(), balance.getAmount(), balance.getCurrencyCode());
        return response;
    }

    private BalanceResponse buildResponse() {
        long amount = ThreadLocalRandom.current().nextLong(1000);
        Balance balance = new Balance()
                .setCurrencyCode("EUR")
                .setAmount(amount);
        AccountReference accountReference = new AccountReference()
                .setId(amount > 500L ? MOCK_ACCOUNT_ID_ONE : MOCK_ACCOUNT_ID_TWO);
        return new BalanceResponse()
                .setAccountReference(accountReference)
                .setResponseTime(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT))
                .setBalance(balance);
    }
}
