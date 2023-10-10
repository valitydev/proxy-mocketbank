package dev.vality.proxy.mocketbank.handler.oct;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.msgpack.Value;
import dev.vality.damsel.withdrawals.provider_adapter.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import static dev.vality.adapter.common.damsel.WithdrawalsProviderVerification.isSuccess;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OctServerHandlerTest extends OctIntegrationTest {

    @MockBean
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
        assertTrue("Result processWithdrawal isn`t success", isSuccess(result));
    }

}
