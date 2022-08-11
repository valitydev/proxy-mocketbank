package com.rbkmoney.proxy.mocketbank.handler.oct;

import com.rbkmoney.cds.storage.CardData;
import com.rbkmoney.cds.storage.StorageSrv;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.msgpack.Value;
import com.rbkmoney.damsel.withdrawals.provider_adapter.ProcessResult;
import com.rbkmoney.proxy.mocketbank.utils.PayoutCardListUtils;
import com.rbkmoney.proxy.mocketbank.utils.payout.CardPayoutAction;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static com.rbkmoney.proxy.mocketbank.TestData.createCardData;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OctServerHandlerFailTest extends OctIntegrationTest {

    @MockBean
    private StorageSrv.Iface storageSrv;

    @Test
    void testProcessWithdrawal() throws Exception {
        List<String> pans = PayoutCardListUtils.extractPans(cardPayoutList, CardPayoutAction::isCardFailed);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processWithdrawalFail(cardData);
        }
    }

    private void processWithdrawalFail(CardData cardData) throws Exception {
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
        assertTrue("Result processWithdrawal isn`t success", isFailure(result));
    }

    public static boolean isFailure(ProcessResult processResult) {
        return processResult.getIntent().getFinish().getStatus().isSetFailure();
    }

}
