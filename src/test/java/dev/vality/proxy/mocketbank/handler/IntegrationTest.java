package dev.vality.proxy.mocketbank.handler;

import dev.vality.adapter.common.cds.CdsPackageCreators;
import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.common.damsel.DomainPackageCreators;
import dev.vality.cds.storage.CardData;
import dev.vality.damsel.domain.*;
import dev.vality.damsel.proxy_provider.*;
import dev.vality.damsel.proxy_provider.Cash;
import dev.vality.damsel.proxy_provider.InvoicePaymentRefund;
import dev.vality.proxy.mocketbank.TestData;
import dev.vality.proxy.mocketbank.decorator.PaymentServerHandlerMdcLog;
import dev.vality.proxy.mocketbank.service.mpi.MpiApi;
import dev.vality.proxy.mocketbank.service.mpi.constant.EnrollmentStatus;
import dev.vality.proxy.mocketbank.service.mpi.constant.TransactionStatus;
import dev.vality.proxy.mocketbank.service.mpi.model.ValidatePaResResponse;
import dev.vality.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
import dev.vality.proxy.mocketbank.service.mpi20.Mpi20Client;
import dev.vality.proxy.mocketbank.service.mpi20.model.AuthenticationResponse;
import dev.vality.proxy.mocketbank.service.mpi20.model.Error;
import dev.vality.proxy.mocketbank.service.mpi20.model.PreparationResponse;
import dev.vality.proxy.mocketbank.service.mpi20.model.ResultResponse;
import dev.vality.proxy.mocketbank.utils.model.Card;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.vality.adapter.common.damsel.DomainPackageCreators.*;
import static dev.vality.adapter.common.damsel.DomainPackageCreators.createDisposablePaymentResource;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createInvoice;
import static dev.vality.proxy.mocketbank.TestData.DEFAULT_THREE_DS_TRANS_ID;
import static dev.vality.proxy.mocketbank.TestData.DEFAULT_THREE_METHOD_DATA;
import static dev.vality.proxy.mocketbank.service.mpi20.constant.CallbackResponseFields.CRES;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@Slf4j
@RequiredArgsConstructor
public abstract class IntegrationTest {

    protected String invoiceId = "TEST_INVOICE";
    protected String paymentId = "TEST_PAYMENT";
    protected String recurrentId = "TEST_RECURRENT";
    protected String refundId = "TEST_REFUND";

    @Autowired
    protected PaymentServerHandlerMdcLog handler;

    @Autowired
    protected List<Card> cardList;

    @MockitoBean
    protected CdsStorageClient cdsStorage;

    @MockitoBean
    protected MpiApi mpiApi;

    @MockitoBean
    protected Mpi20Client mpi20Client;

    protected Map<String, String> prepareProxyOptions() {
        return new HashMap<>();
    }

    protected Shop prepareShop() {
        ShopLocation shopLocation = new ShopLocation();
        shopLocation.setUrl("url");
        return new Shop()
                .setId("shop_id")
                .setCategory(new Category().setName("CategoryName").setDescription("Category description"))
                .setName("ShopName")
                .setDescription("Shop description")
                .setLocation(shopLocation);
    }

    protected Cash prepareCash() {
        return DomainPackageCreators.createCash(
                10000L, "Rubles", 643, "RUB", 2);
    }

    protected PaymentInfo getPaymentInfo(String sessionId,
                                         BankCard bankCard,
                                         TransactionInfo transactionInfo) {
        PaymentResource paymentResource = getPaymentResource(sessionId, bankCard);
        PaymentInfo paymentInfo = getPaymentInfo(transactionInfo, paymentResource);
        paymentInfo.setCapture(prepareInvoicePaymentCapture());
        paymentInfo.setRefund(createInvoicePaymentRefund(transactionInfo));
        return paymentInfo;
    }

    protected PaymentInfo getPaymentInfo(TransactionInfo transactionInfo, PaymentResource paymentResource) {
        PaymentInfo paymentInfo = createPaymentInfo(
                createInvoice(
                        invoiceId,
                        TestData.CREATED_AT,
                        prepareCash()
                ),
                prepareShop(),
                createInvoicePaymentWithTrX(
                        paymentId,
                        TestData.CREATED_AT,
                        paymentResource,
                        prepareCash(),
                        transactionInfo
                ).setMakeRecurrent(Boolean.FALSE));

        paymentInfo.setCapture(prepareInvoicePaymentCapture());
        paymentInfo.setRefund(createInvoicePaymentRefund(transactionInfo));
        return paymentInfo;
    }

    private PayerSessionInfo createPayerSessionInfo(String redirectUrl) {
        return new PayerSessionInfo()
                .setRedirectUrl(redirectUrl);
    }

    private InvoicePaymentRefund createInvoicePaymentRefund(TransactionInfo transactionInfo) {
        InvoicePaymentRefund invoicePaymentRefund = new InvoicePaymentRefund();
        invoicePaymentRefund.setId(refundId);
        invoicePaymentRefund.setTrx(transactionInfo);
        return invoicePaymentRefund;
    }

    private InvoicePaymentCapture prepareInvoicePaymentCapture() {
        InvoicePaymentCapture invoicePaymentCapture = new InvoicePaymentCapture();
        invoicePaymentCapture.setCost(prepareCash());
        return invoicePaymentCapture;
    }

    protected PaymentResource getPaymentResource(String sessionId, BankCard bankCard) {
        return createPaymentResourceDisposablePaymentResource(
                createDisposablePaymentResource(
                        createClientInfo(TestData.FINGERPRINT, TestData.IP_ADDRESS),
                        sessionId,
                        createPaymentTool(bankCard)
                )
        );
    }

    protected PaymentContext getContext(
            BankCard bankCard,
            TargetInvoicePaymentStatus target,
            TransactionInfo transactionInfo,
            String redirectUrl
    ) {
        PaymentContext context = getContext(bankCard, target, transactionInfo);
        context.getPaymentInfo().getPayment().setPayerSessionInfo(createPayerSessionInfo(redirectUrl));
        return context;
    }

    protected PaymentContext getContext(
            BankCard bankCard,
            TargetInvoicePaymentStatus target,
            TransactionInfo transactionInfo
    ) {
        byte[] state = new byte[0];
        return createContext(
                getPaymentInfo(TestData.SESSION_ID, bankCard, transactionInfo),
                createSession(target, state),
                prepareProxyOptions()
        );
    }

    protected PaymentContext getContext(
            PaymentResource paymentResource,
            TargetInvoicePaymentStatus target,
            TransactionInfo transactionInfo
    ) {
        byte[] state = new byte[0];
        return createContext(
                getPaymentInfo(transactionInfo, paymentResource),
                createSession(target, state),
                prepareProxyOptions()
        );
    }

    protected void mockCds(CardData cardData, BankCard bankCard) {
        CardDataProxyModel proxyModel = CardDataProxyModel.builder()
                .cardholderName(bankCard.getCardholderName())
                .expMonth(bankCard.getExpDate().getMonth())
                .expYear(bankCard.getExpDate().getYear())
                .pan(cardData.getPan())
                .build();

        Mockito.when(cdsStorage.getCardData(anyString())).thenReturn(cardData);
        Mockito.when(cdsStorage.getCardData((PaymentContext) any())).thenReturn(proxyModel);
        Mockito.when(cdsStorage.getSessionData(any()))
                .thenReturn(CdsPackageCreators.createSessionDataWithCvv(TestData.DEFAULT_CVV));
    }

    protected void mockMpiVerify(EnrollmentStatus mpiEnrollmentStatus) {
        VerifyEnrollmentResponse response = new VerifyEnrollmentResponse();
        response.setAcsUrl(TestData.DEFAULT_ACS_URL);
        response.setEnrolled(mpiEnrollmentStatus.getStatus());
        response.setPaReq(TestData.DEFAULT_PAREQ);
        Mockito.when(mpiApi.verifyEnrollment((CardDataProxyModel) any())).thenReturn(response);
    }

    protected void mockMpi(TransactionStatus mpiTransactionStatus) {
        ValidatePaResResponse paResResponse = new ValidatePaResResponse();
        paResResponse.setTransactionStatus(mpiTransactionStatus.getStatus());
        Mockito.when(mpiApi.validatePaRes(any(), any())).thenReturn(paResResponse);
    }

    @SneakyThrows
    protected void mockMpiV2Prepare() {
        PreparationResponse response = new PreparationResponse();
        response.setProtocolVersion("2");
        response.setThreeDSMethodURL(TestData.DEFAULT_MPIV2_PREPARE_URL);
        response.setThreeDSMethodData(DEFAULT_THREE_METHOD_DATA);
        response.setThreeDSServerTransID(DEFAULT_THREE_DS_TRANS_ID);
        response.setError(new Error());
        Mockito.when(mpi20Client.prepare(any())).thenReturn(response);
    }

    protected void mockMpiV2Auth() {
        AuthenticationResponse response = new AuthenticationResponse();
        response.setThreeDSServerTransID(DEFAULT_THREE_DS_TRANS_ID);
        response.setTransStatus("C");
        response.setAcsUrl(TestData.DEFAULT_MPIV2_ACS_URL);
        response.setCreq(CRES);
        response.setError(new Error());
        Mockito.when(mpi20Client.auth(any())).thenReturn(response);
    }

    protected void mockMpiV2Result() {
        ResultResponse response = new ResultResponse();
        response.setThreeDSServerTransID(DEFAULT_THREE_DS_TRANS_ID);
        response.setTransStatus("Y");
        response.setError(new Error());
        Mockito.when(mpi20Client.result(any())).thenReturn(response);
    }

}
