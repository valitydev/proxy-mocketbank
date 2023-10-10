package dev.vality.proxy.mocketbank.handler.payment.recurrent;

import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.common.exception.CdsStorageExpDateException;
import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.damsel.domain.Failure;
import dev.vality.damsel.domain.OperationFailure;
import dev.vality.damsel.proxy_provider.RecurrentTokenContext;
import dev.vality.damsel.proxy_provider.RecurrentTokenIntent;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
import dev.vality.damsel.timeout_behaviour.TimeoutBehaviour;
import dev.vality.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import dev.vality.proxy.mocketbank.configuration.properties.TimerProperties;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.service.mpi.MpiApi;
import dev.vality.proxy.mocketbank.service.mpi.constant.EnrollmentStatus;
import dev.vality.proxy.mocketbank.service.mpi.model.VerifyEnrollmentResponse;
import dev.vality.proxy.mocketbank.utils.ErrorBuilder;
import dev.vality.proxy.mocketbank.utils.TokenProviderVerification;
import dev.vality.proxy.mocketbank.utils.UrlUtils;
import dev.vality.proxy.mocketbank.utils.model.Card;
import dev.vality.proxy.mocketbank.utils.model.CardAction;
import dev.vality.proxy.mocketbank.utils.model.CardUtils;
import dev.vality.proxy.mocketbank.utils.state.StateUtils;
import dev.vality.proxy.mocketbank.utils.state.constant.SuspendPrefix;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.vality.adapter.common.damsel.OptionsExtractors.extractRedirectTimeout;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageExtractors.extractRecurrentId;
import static dev.vality.adapter.common.damsel.model.Error.DEFAULT_ERROR_CODE;
import static dev.vality.adapter.common.damsel.model.Error.THREE_DS_NOT_FINISHED;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateTokenHandler {

    private final CdsStorageClient cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;
    private final TimerProperties timerProperties;
    private final AdapterMockBankProperties mockBankProperties;

    public RecurrentTokenProxyResult handler(RecurrentTokenContext context) {
        String recurrentId = extractRecurrentId(context);
        RecurrentTokenIntent intent = createRecurrentTokenFinishIntentSuccess(recurrentId);

        // Applepay, Samsungpay, Googlepay - always successful and does not depends on card
        if (TokenProviderVerification.hasBankCardTokenProvider(context)) {
            return createRecurrentTokenProxyResult(intent);
        }

        CardDataProxyModel cardData;
        try {
            cardData = cds.getCardData(context);
        } catch (CdsStorageExpDateException ex) {
            return ErrorBuilder.prepareRecurrentTokenError(errorMapping, DEFAULT_ERROR_CODE, ex.getMessage());
        }

        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        if (card.isPresent()) {
            CardAction action = CardAction.findByValue(card.get().getAction());
            if (CardAction.isCardEnrolled(card.get())) {
                return prepareEnrolledRecurrentTokenProxyResult(context, intent, cardData);
            }
            return prepareNotEnrolledRecurrentTokenProxyResult(intent, action);
        }
        return ErrorBuilder.prepareRecurrentTokenError(errorMapping, CardAction.UNSUPPORTED_CARD);
    }

    private RecurrentTokenProxyResult prepareNotEnrolledRecurrentTokenProxyResult(
            RecurrentTokenIntent intent,
            CardAction action) {

        if (CardAction.isCardSuccess(action)) {
            return createRecurrentTokenProxyResult(intent, PaymentState.CAPTURED.getBytes());
        }
        CardAction currentAction = CardAction.isCardFailed(action) ? action : CardAction.UNKNOWN_FAILURE;
        return ErrorBuilder.prepareRecurrentTokenError(errorMapping, currentAction);
    }

    private RecurrentTokenProxyResult prepareEnrolledRecurrentTokenProxyResult(
            RecurrentTokenContext context,
            RecurrentTokenIntent intent,
            CardDataProxyModel cardData) {
        RecurrentTokenIntent recurrentTokenIntent = intent;
        VerifyEnrollmentResponse verifyEnrollmentResponse = mpiApi.verifyEnrollment(cardData);
        if (EnrollmentStatus.isAuthenticationAvailable(verifyEnrollmentResponse.getEnrolled())) {
            String tag = SuspendPrefix.RECURRENT.getPrefix() + context.getTokenInfo().getPaymentTool().getId();
            String termUrl = UrlUtils.getCallbackUrl(
                    mockBankProperties.getCallbackUrl(),
                    mockBankProperties.getPathRecurrentCallbackUrl());
            recurrentTokenIntent = prepareRedirect(context, verifyEnrollmentResponse, tag, termUrl);
        }
        byte[] state = StateUtils.prepareState(verifyEnrollmentResponse);
        return createRecurrentTokenProxyResult(recurrentTokenIntent, state);
    }

    private RecurrentTokenIntent prepareRedirect(
            RecurrentTokenContext context,
            VerifyEnrollmentResponse verifyEnrollmentResponse,
            String tag,
            String termUrl) {
        String url = verifyEnrollmentResponse.getAcsUrl();
        Map<String, String> params = UrlUtils.prepareRedirectParams(verifyEnrollmentResponse, tag, termUrl);
        Map<String, String> options = context.getOptions();
        int timerRedirectTimeout = extractRedirectTimeout(options, timerProperties.getRedirectTimeout());

        RecurrentTokenIntent recurrentTokenIntent = createRecurrentTokenWithSuspendIntent(
                tag, timerRedirectTimeout, createPostUserInteraction(url, params)
        );
        Failure failure = errorMapping.mapFailure(DEFAULT_ERROR_CODE, THREE_DS_NOT_FINISHED);
        recurrentTokenIntent.getSuspend().setTimeoutBehaviour(
                TimeoutBehaviour.operation_failure(OperationFailure.failure(failure)));
        return recurrentTokenIntent;
    }
}
