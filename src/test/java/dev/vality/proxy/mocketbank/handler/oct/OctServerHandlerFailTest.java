package dev.vality.proxy.mocketbank.handler.oct;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.msgpack.Value;
import dev.vality.damsel.withdrawals.provider_adapter.ProcessResult;
import dev.vality.proxy.mocketbank.utils.PayoutCardListUtils;
import dev.vality.proxy.mocketbank.utils.payout.CardPayoutAction;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static dev.vality.proxy.mocketbank.TestData.createCardData;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class OctServerHandlerFailTest extends OctIntegrationTest {

    @MockBean
    private CdsStorageClient cdsStorageClient;

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
        when(cdsStorageClient.getCardData(anyString())).thenReturn(cardData);

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
