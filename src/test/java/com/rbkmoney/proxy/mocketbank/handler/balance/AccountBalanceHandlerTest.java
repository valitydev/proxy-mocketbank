package com.rbkmoney.proxy.mocketbank.handler.balance;

import dev.vality.scrooge.BalanceRequest;
import dev.vality.scrooge.BalanceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class AccountBalanceHandlerTest {

    private final AccountBalanceHandler handler = new AccountBalanceHandler();

    @Test
    void getBalanceWithError() {
        BalanceRequest request = new BalanceRequest();
        request.setOptions(Collections.emptyMap());

        var exception = assertThrows(IllegalArgumentException.class, () -> handler.getBalance(request));

        assertEquals("Empty options in account balance request", exception.getMessage());
    }

    @Test
    void getBalanceSuccess() {
        BalanceRequest request = new BalanceRequest();
        request.setOptions(Map.of(UUID.randomUUID().toString(), UUID.randomUUID().toString()));

        BalanceResponse response = handler.getBalance(request);

        assertNotNull(response);
    }
}