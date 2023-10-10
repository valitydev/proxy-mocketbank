package dev.vality.proxy.mocketbank.handler;

import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.PaymentSystemRef;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.proxy.mocketbank.TestData;
import dev.vality.proxy.mocketbank.utils.CardListUtils;
import dev.vality.proxy.mocketbank.utils.model.CardAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static dev.vality.adapter.common.damsel.DomainPackageCreators.createTargetProcessed;
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
public class MocketBankServerHandlerSuccessGooglePayIntegrationTest extends IntegrationTest {

    @Test
    void testProcessPaymentSuccess() throws TException {
        List<String> pans = CardListUtils.extractPans(cardList, CardAction::isCardSuccessGooglePay);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processPayment(cardData);
        }
    }

    private void processPayment(CardData cardData) throws TException {
        BankCard bankCard = TestData.createBankCard(cardData);
        bankCard.setPaymentSystem(new PaymentSystemRef("googlepay"));
        mockCds(cardData, bankCard);

        PaymentProxyResult proxyResult = handler.processPayment(getContext(bankCard, createTargetProcessed(), null));
        assertTrue("Process payment isn`t success", isSuccess(proxyResult));
    }

}
