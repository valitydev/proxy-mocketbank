package dev.vality.proxy.mocketbank.handler;

import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.proxy.mocketbank.TestData;
import dev.vality.proxy.mocketbank.utils.CardListUtils;
import dev.vality.proxy.mocketbank.utils.TestConstants;
import dev.vality.proxy.mocketbank.utils.model.CardAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.List;

import static dev.vality.adapter.common.damsel.DomainPackageCreators.*;
import static dev.vality.adapter.common.damsel.ProxyProviderVerification.isSuccess;
import static dev.vality.proxy.mocketbank.TestData.createCardData;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.storage.url=http://127.0.0.1:8021/v1/storage",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MocketBankServerHandlerSuccessIntegrationTest extends IntegrationTest {

    @Test
    void testProcessPaymentSuccess() throws TException {
        List<String> pans = CardListUtils.extractPans(cardList, CardAction::isCardSuccess);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processPayment(cardData, null);
        }
    }

    @Test
    void testProcessPaymentSuccessWithRedirect() throws TException {
        List<String> pans = CardListUtils.extractPans(cardList, CardAction::isCardSuccess);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processPayment(cardData, TestConstants.REDIRECT_URL);
        }
    }

    private void processPayment(CardData cardData, String redirectUrl) throws TException {
        BankCard bankCard = TestData.createBankCard(cardData);
        mockCds(cardData, bankCard);

        PaymentProxyResult result = handler.processPayment(
                getContext(bankCard, createTargetProcessed(), null, redirectUrl));
        assertTrue("Process payment isn`t success", isSuccess(result));

        TransactionInfo trxInfo = createTransactionInfo(result.getTrx().getId(), Collections.emptyMap());
        result = handler.processPayment(
                getContext(bankCard, createTargetCaptured(), trxInfo, redirectUrl));
        assertTrue("Process Capture isn`t success", isSuccess(result));
    }

}
