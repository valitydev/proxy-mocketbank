package com.rbkmoney.proxy.mocketbank.handler.p2p;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.p2p_adapter.Context;
import com.rbkmoney.damsel.p2p_adapter.ProcessResult;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.utils.CardListUtils;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.rbkmoney.java.damsel.utils.verification.P2pAdapterVerification.isSuccess;
import static com.rbkmoney.proxy.mocketbank.TestData.createCardData;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class P2pServerHandlerSuccessTest extends P2PIntegrationTest {

    @Test
    public void testProcessSuccess() throws TException {
        List<String> pans = CardListUtils.extractPans(cardList, CardAction::isCardSuccess);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            process(cardData);
        }
    }

    private void process(CardData cardData) throws TException {
        BankCard bankCard = TestData.createBankCard(cardData);
        mockCds(cardData, bankCard);

        Context context = createContext(bankCard);
        ProcessResult result = handler.process(context);
        assertTrue("Process payment isn`t success", isSuccess(result));
    }

}