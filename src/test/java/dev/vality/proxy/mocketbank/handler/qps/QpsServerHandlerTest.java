package dev.vality.proxy.mocketbank.handler.qps;

import dev.vality.damsel.domain.DisposablePaymentResource;
import dev.vality.damsel.domain.PaymentServiceRef;
import dev.vality.damsel.domain.PaymentTerminal;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.proxy.mocketbank.TestData;
import dev.vality.proxy.mocketbank.handler.IntegrationTest;
import dev.vality.proxy.mocketbank.handler.terminal.TerminalServerHandler;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static dev.vality.adapter.common.damsel.DomainPackageCreators.*;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createPaymentResourceDisposablePaymentResource;
import static dev.vality.adapter.common.damsel.ProxyProviderVerification.isSleep;
import static dev.vality.adapter.common.damsel.ProxyProviderVerification.isSuccess;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class QpsServerHandlerTest extends IntegrationTest {

    @Autowired
    private TerminalServerHandler handler;

    @Test
    void testProcess() throws TException {
        PaymentTerminal paymentTerminal = new PaymentTerminal();
        paymentTerminal.setPaymentService(new PaymentServiceRef("qps"));
        PaymentTool paymentTool = PaymentTool.payment_terminal(paymentTerminal);
        DisposablePaymentResource disposablePaymentResource = createDisposablePaymentResource(
                createClientInfo(TestData.FINGERPRINT, TestData.IP_ADDRESS),
                TestData.SESSION_ID,
                paymentTool
        );
        PaymentResource paymentResource = createPaymentResourceDisposablePaymentResource(disposablePaymentResource);

        PaymentContext paymentContext = getContext(paymentResource, createTargetProcessed(), null);
        PaymentProxyResult proxyResult = handler.processPayment(paymentContext);
        assertTrue("Qps Terminal processPayment isn`t sleep", isSleep(proxyResult));

        paymentContext.getSession().setState(proxyResult.getNextState());
        paymentContext.getPaymentInfo().getPayment().setTrx(proxyResult.getTrx());
        proxyResult = handler.processPayment(paymentContext);
        assertTrue("Qps Terminal processPayment isn`t sleep", isSuccess(proxyResult));

        paymentContext.getSession().setTarget(createTargetCaptured());
        proxyResult = handler.processPayment(paymentContext);
        assertTrue("Process Capture isn`t success", isSuccess(proxyResult));
    }

}
