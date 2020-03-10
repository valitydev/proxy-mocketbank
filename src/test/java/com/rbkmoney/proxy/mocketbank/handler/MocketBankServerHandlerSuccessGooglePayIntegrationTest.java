package com.rbkmoney.proxy.mocketbank.handler;

import com.rbkmoney.damsel.cds.CardData;
import com.rbkmoney.damsel.domain.BankCard;
import com.rbkmoney.damsel.domain.BankCardTokenProvider;
import com.rbkmoney.damsel.proxy_provider.PaymentProxyResult;
import com.rbkmoney.proxy.mocketbank.TestData;
import com.rbkmoney.proxy.mocketbank.utils.constant.testcards.Mastercard;
import com.rbkmoney.proxy.mocketbank.utils.constant.testcards.TestCard;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static com.rbkmoney.java.damsel.utils.creators.DomainPackageCreators.createTargetProcessed;
import static com.rbkmoney.java.damsel.utils.verification.ProxyProviderVerification.isSuccess;
import static com.rbkmoney.proxy.mocketbank.TestData.createCardData;
import static org.junit.Assert.assertTrue;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.storage.url=http://127.0.0.1:8021/v1/storage",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MocketBankServerHandlerSuccessGooglePayIntegrationTest extends IntegrationTest  {

    @Test
    public void testProcessPaymentSuccess() throws TException {
        TestCard[] cards = {
                Mastercard.GOOGLE_PAY_SUCCESS,
        };

        for (TestCard card : cards) {
            CardData cardData = createCardData(card.getCardNumber());
            processPayment(cardData);
        }
    }

    private void processPayment(CardData cardData) throws TException {
        BankCard bankCard = TestData.createBankCard(cardData);
        bankCard.setTokenProvider(BankCardTokenProvider.googlepay);
        mockCds(cardData, bankCard);

        PaymentProxyResult proxyResult = handler.processPayment(getContext(bankCard, createTargetProcessed(), null));
        assertTrue("Process payment isn`t success", isSuccess(proxyResult));
    }

}
