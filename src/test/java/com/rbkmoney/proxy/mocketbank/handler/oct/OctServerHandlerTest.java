package com.rbkmoney.proxy.mocketbank.handler.oct;

import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.cds.storage.StorageSrv;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import static com.rbkmoney.java.damsel.utils.verification.WithdrawalsProviderVerification.isSuccess;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OctServerHandlerTest extends OctIntegrationTest {

    @MockBean
    private StorageSrv.Iface storageSrv;

    @Test
    void testProcessWithdrawal() throws Exception {
        CardData cardData = new CardData()
                .setPan(randomString())
                .setCardholderName(randomString());
        BankCard bankCard = new BankCard()
                .setToken(randomString())
                .setCardholderName(randomString())
                .setBin(randomString());
        when(storageSrv.getCardData(anyString())).thenReturn(cardData);

        ProcessResult result = handler.processWithdrawal(
                createWithdrawal(bankCard),
                Value.str(""),
                createProxyOptions()
        );
        log.info("Response processWithdrawal {}", result);
        assertTrue("Result processWithdrawal isn`t success", isSuccess(result));
    }

}
