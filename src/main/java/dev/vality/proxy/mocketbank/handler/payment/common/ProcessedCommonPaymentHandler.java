package dev.vality.proxy.mocketbank.handler.payment.common;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.common.damsel.ProxyProviderPackageCreators;
import dev.vality.adapter.common.exception.CdsStorageExpDateException;
import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.damsel.domain.Failure;
import dev.vality.damsel.domain.OperationFailure;
import dev.vality.damsel.domain.TargetInvoicePaymentStatus;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.Intent;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.PaymentResource;
import dev.vality.damsel.timeout_behaviour.TimeoutBehaviour;
import dev.vality.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import dev.vality.proxy.mocketbank.configuration.properties.TimerProperties;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.handler.payment.CommonPaymentHandler;
import dev.vality.proxy.mocketbank.service.mpi.MpiApi;
import dev.vality.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
import dev.vality.proxy.mocketbank.service.mpi20.processor.Mpi20Processor;
import dev.vality.proxy.mocketbank.utils.CreatorUtils;
import dev.vality.proxy.mocketbank.utils.ErrorBuilder;
import dev.vality.proxy.mocketbank.utils.UrlUtils;
import dev.vality.proxy.mocketbank.utils.UserInteractionUtils;
import dev.vality.proxy.mocketbank.utils.model.Card;
import dev.vality.proxy.mocketbank.utils.model.CardAction;
import dev.vality.proxy.mocketbank.utils.model.CardUtils;
import dev.vality.proxy.mocketbank.utils.state.StateUtils;
import dev.vality.proxy.mocketbank.utils.state.constant.SuspendPrefix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.vality.adapter.common.damsel.OptionsExtractors.extractRedirectTimeout;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractInvoiceId;
import static dev.vality.adapter.common.damsel.ProxyProviderVerification.isMakeRecurrent;
import static dev.vality.adapter.common.damsel.model.Error.DEFAULT_ERROR_CODE;
import static dev.vality.adapter.common.damsel.model.Error.THREE_DS_NOT_FINISHED;
import static dev.vality.proxy.mocketbank.service.mpi.constant.EnrollmentStatus.isAuthenticationAvailable;
import static dev.vality.proxy.mocketbank.utils.TokenProviderVerification.hasBankCardTokenProvider;
import static dev.vality.proxy.mocketbank.utils.UrlUtils.prepareRedirectParams;
import static dev.vality.proxy.mocketbank.utils.model.CardAction.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcessedCommonPaymentHandler implements CommonPaymentHandler {

    private final CdsStorageClient cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;
    private final TimerProperties timerProperties;
    private final AdapterMockBankProperties mockBankProperties;
    private final Mpi20Processor mpi20Processor;

    @Override
    public boolean filter(TargetInvoicePaymentStatus targetInvoicePaymentStatus, PaymentResource paymentResource) {
        return targetInvoicePaymentStatus.isSetProcessed() && paymentResource.isSetDisposablePaymentResource();
    }

    @Override
    public PaymentProxyResult handler(PaymentContext context) throws TException {
        Intent intent = createFinishIntentSuccess();
        if (isMakeRecurrent(context)) {
            String invoiceId = extractInvoiceId(context);
            intent = createFinishIntentSuccessWithToken(invoiceId);
        }

        // Applepay, Samsungpay, Googlepay - always successful and does not depends on card
        TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);
        if (hasBankCardTokenProvider(context)) {
            return createPaymentProxyResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
        }

        CardDataProxyModel cardData;
        try {
            cardData = cds.getCardData(context);
        } catch (CdsStorageExpDateException ex) {
            return ErrorBuilder.prepareError(errorMapping, DEFAULT_ERROR_CODE, ex.getMessage());
        }

        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        if (card.isPresent()) {
            CardAction action = CardAction.findByValue(card.get().getAction());
            if (CardAction.isCardEnrolled(card.get())) {
                return prepareEnrolledPaymentProxyResult(context, intent, transactionInfo, cardData, action);
            } else if (CardAction.isCardEnrolled20(card.get())) {
                return mpi20Processor.processPrepare(context, action);
            }
            return prepareNotEnrolledPaymentProxyResult(intent, transactionInfo, action);
        }
        return ErrorBuilder.prepareError(errorMapping, UNSUPPORTED_CARD);
    }

    private PaymentProxyResult prepareNotEnrolledPaymentProxyResult(
            Intent intent,
            TransactionInfo transactionInfo,
            CardAction action) {
        if (isCardSuccess(action)) {
            return createPaymentProxyResult(intent, PaymentState.CAPTURED.getBytes(), transactionInfo);
        }
        CardAction currentAction = isCardFailed(action) ? action : UNKNOWN_FAILURE;
        return ErrorBuilder.prepareError(errorMapping, currentAction);
    }

    private PaymentProxyResult prepareEnrolledPaymentProxyResult(
            PaymentContext context,
            Intent intent,
            TransactionInfo transactionInfo,
            CardDataProxyModel cardData,
            CardAction action) {
        Intent currentIntent = intent;
        VerifyEnrollmentResponse verifyEnrollmentResponse = mpiApi.verifyEnrollment(cardData);
        if (isAuthenticationAvailable(verifyEnrollmentResponse.getEnrolled())) {
            String tag = SuspendPrefix.PAYMENT.getPrefix() +
                    ProxyProviderPackageCreators.createInvoiceWithPayment(context.getPaymentInfo());
            MultiValueMap<String, String> terminationUrl = UrlUtils.getTerminationUrlAsParam(
                    context.getPaymentInfo().getPayment(),
                    mockBankProperties.getFinishInteraction()
            );
            String termUrl = UrlUtils.getCallbackUrl(
                    mockBankProperties.getCallbackUrl(),
                    mockBankProperties.getPathCallbackUrl(),
                    terminationUrl
            );
            currentIntent = prepareRedirect(context, verifyEnrollmentResponse, tag, termUrl, action);
        }
        byte[] state = StateUtils.prepareState(verifyEnrollmentResponse);
        return createPaymentProxyResult(currentIntent, state, transactionInfo);
    }

    private Intent prepareRedirect(
            PaymentContext context,
            VerifyEnrollmentResponse verifyEnrollmentResponse,
            String tag,
            String termUrl,
            CardAction action) {
        String url = verifyEnrollmentResponse.getAcsUrl();
        Map<String, String> params = prepareRedirectParams(verifyEnrollmentResponse, tag, termUrl);
        Map<String, String> options = context.getOptions();
        int timerRedirectTimeout = extractRedirectTimeout(options, timerProperties.getRedirectTimeout());

        Intent intent = createIntentWithSuspendIntent(
                tag, timerRedirectTimeout, UserInteractionUtils.getUserInteraction(url, params, action)
        );
        Failure failure = errorMapping.mapFailure(DEFAULT_ERROR_CODE, THREE_DS_NOT_FINISHED);
        intent.getSuspend().setTimeoutBehaviour(TimeoutBehaviour.operation_failure(OperationFailure.failure(failure)));
        return intent;
    }

}
