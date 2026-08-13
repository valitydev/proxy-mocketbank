package dev.vality.proxy.mocketbank.handler.oct;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.msgpack.Value;
import dev.vality.damsel.withdrawals.provider_adapter.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static dev.vality.adapter.common.damsel.WithdrawalsProviderVerification.isSuccess;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OctServerHandlerTest extends OctIntegrationTest {

    @MockitoBean
    private CdsStorageClient cdsStorageClient;

    @Test
    void testProcessWithdrawal() throws Exception {
        CardData cardData = new CardData()
                .setPan(randomString())
                .setCardholderName(randomString());
        BankCard bankCard = new BankCard()
                .setToken(randomString())
                .setCardholderName(randomString())
                .setBin(randomString());
        when(cdsStorageClient.getCardData(anyString())).thenReturn(cardData);

        ProcessResult result = handler.processWithdrawal(
                createWithdrawal(bankCard),
                Value.str(""),
                createProxyOptions()
        );
        log.info("Response processWithdrawal {}", result);
        assertTrue(isSuccess(result), "Result processWithdrawal isn`t success");
    }

    @Test
    void testProcessWithdrawalWithChangedAmount() throws Exception {
        CardData cardData = new CardData()
                .setPan("4000000000000077")
                .setCardholderName(randomString());
        BankCard bankCard = new BankCard()
                .setToken(randomString())
                .setCardholderName(randomString())
                .setBin(randomString());
        when(cdsStorageClient.getCardData(anyString())).thenReturn(cardData);

        var withdrawal = createWithdrawal(bankCard);
        ProcessResult result = handler.processWithdrawal(
                withdrawal,
                Value.str(""),
                createProxyOptions()
        );

        assertTrue(isSuccess(result), "Result processWithdrawal isn`t success");
        assertTrue(result.isSetNewBody(), "Result processWithdrawal doesn`t contain new body");
        assertEquals(withdrawal.getBody().getAmount() / 2, result.getNewBody().getAmount());
        assertEquals(withdrawal.getBody().getCurrency(), result.getNewBody().getCurrency());
    }

}
