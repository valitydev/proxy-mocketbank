package dev.vality.proxy.mocketbank.handler;

import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
import dev.vality.proxy.mocketbank.TestData;
import dev.vality.proxy.mocketbank.utils.CardListUtils;
import dev.vality.proxy.mocketbank.utils.model.CardAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.List;

import static dev.vality.adapter.common.damsel.DomainPackageCreators.createTargetCaptured;
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
public class MocketBankServerHandlerRecurrentSuccessIntegrationTest extends IntegrationTest {

    @Test
    void testProcessPaymentSuccess() throws TException, IOException {
        List<String> pans = CardListUtils.extractPans(cardList, CardAction::isCardSuccess);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processPayment(cardData);
        }
    }

    private void processPayment(CardData cardData) throws TException {
        BankCard bankCard = TestData.createBankCard(cardData);
        bankCard.setToken(TestData.BANK_CARD_TOKEN);
        mockCds(cardData, bankCard);

        RecurrentTokenContext context = createRecurrentTokenContext(bankCard);
        RecurrentTokenProxyResult generationProxyResult = handler.generateToken(context);

        String token = generationProxyResult.getIntent().getFinish().getStatus().getSuccess().getToken();

        PaymentContext paymentContext = getContext(getPaymentResourceRecurrent(token), createTargetProcessed(), null);
        PaymentProxyResult proxyResult = handler.processPayment(paymentContext);
        assertTrue("Process Payment isn`t success", isSuccess(proxyResult));

        paymentContext.getPaymentInfo().getPayment().setTrx(proxyResult.getTrx());
        paymentContext.getSession().setTarget(createTargetCaptured());

        PaymentProxyResult processResultCapture = handler.processPayment(paymentContext);
        assertTrue("Process Capture isn`t success ", isSuccess(processResultCapture));
    }

}
