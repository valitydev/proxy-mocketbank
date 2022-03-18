package com.rbkmoney.proxy.mocketbank.handler.balance;

import dev.vality.account_balance.*;
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
        Balance balance = new Balance();
        balance.setCurrencyCode("EUR");
        long amount = ThreadLocalRandom.current().nextLong(1000);
        balance.setAmount(amount);
        AccountReference accountReference = new AccountReference();
        long accountId = ThreadLocalRandom.current().nextLong(10000);
        accountReference.setId(accountId);
        return new BalanceResponse()
                .setAccountReference(accountReference)
                .setResponseTime(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT))
                .setBalance(balance);
    }
}
