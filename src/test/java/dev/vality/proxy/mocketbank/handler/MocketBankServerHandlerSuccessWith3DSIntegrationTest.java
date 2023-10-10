package dev.vality.proxy.mocketbank.handler;

import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.proxy.mocketbank.TestData;
import dev.vality.proxy.mocketbank.service.mpi.constant.EnrollmentStatus;
import dev.vality.proxy.mocketbank.service.mpi.constant.TransactionStatus;
import dev.vality.proxy.mocketbank.utils.CardListUtils;
import dev.vality.proxy.mocketbank.utils.Converter;
import dev.vality.proxy.mocketbank.utils.TestConstants;
import dev.vality.proxy.mocketbank.utils.model.CardAction;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.vality.adapter.common.damsel.DomainPackageCreators.createTargetCaptured;
import static dev.vality.adapter.common.damsel.DomainPackageCreators.createTargetProcessed;
import static dev.vality.adapter.common.damsel.ProxyProviderVerification.isSuccess;
import static dev.vality.adapter.common.damsel.ProxyProviderVerification.isSuspend;
import static dev.vality.proxy.mocketbank.TestData.createCardData;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@Slf4j
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "cds.client.url.storage.url=http://127.0.0.1:8021/v1/storage",
                "proxy-mocketbank-mpi.url=http://127.0.0.1:8018",
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MocketBankServerHandlerSuccessWith3DSIntegrationTest extends IntegrationTest {

    @Test
    void testProcessPaymentSuccess() throws TException, IOException {
        List<String> pans = CardListUtils.extractPans(cardList, CardAction::isMpiCardSuccess);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processPayment(cardData, null);
        }
    }

    @Test
    void testProcessPaymentSuccessWithRedirect() throws TException, IOException {
        List<String> pans = CardListUtils.extractPans(cardList, CardAction::isMpiCardSuccess);
        for (String pan : pans) {
            CardData cardData = createCardData(pan);
            processPayment(cardData, TestConstants.REDIRECT_URL);
        }
    }

    private void processPayment(CardData cardData, String redirectUrl) throws TException, IOException {
        BankCard bankCard = TestData.createBankCard(cardData);
        mockCds(cardData, bankCard);
        mockMpiVerify(EnrollmentStatus.AUTHENTICATION_AVAILABLE);
        mockMpi(TransactionStatus.AUTHENTICATION_SUCCESSFUL);

        PaymentContext paymentContext = getContext(bankCard, createTargetProcessed(), null, redirectUrl);
        PaymentProxyResult proxyResult = handler.processPayment(paymentContext);
        assertTrue("Process payment isn`t suspend", isSuspend(proxyResult));

        Map<String, String> mapCallback = new HashMap<>();
        mapCallback.put("MD", "MD-TAG");
        mapCallback.put("paRes", "SomePaRes");
        paymentContext.getSession().setState(proxyResult.getNextState());
        ByteBuffer callbackMap = Converter.mapToByteBuffer(mapCallback);

        PaymentCallbackResult callbackResult = handler.handlePaymentCallback(callbackMap, paymentContext);
        assertTrue("CallbackResult isn`t success", isSuccess(callbackResult));

        paymentContext.getSession().setTarget(createTargetCaptured());
        paymentContext.getSession().setState(callbackResult.getResult().getNextState());
        paymentContext.getPaymentInfo().getPayment().setTrx(callbackResult.getResult().getTrx());
        PaymentProxyResult processResultCapture = handler.processPayment(paymentContext);
        assertTrue("Process Capture isn`t success", isSuccess(processResultCapture));
    }

}
