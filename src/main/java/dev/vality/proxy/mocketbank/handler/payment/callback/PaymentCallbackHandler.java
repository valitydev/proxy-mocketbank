package dev.vality.proxy.mocketbank.handler.payment.callback;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.adapter.common.cds.CdsStorageClient;
import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.common.damsel.model.Error;
import dev.vality.adapter.common.exception.CdsStorageExpDateException;
import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.PaymentCallbackProxyResult;
import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
import dev.vality.damsel.proxy_provider.PaymentContext;
import dev.vality.proxy.mocketbank.constant.PaymentState;
import dev.vality.proxy.mocketbank.service.mpi.MpiApi;
import dev.vality.proxy.mocketbank.service.mpi.constant.TransactionStatus;
import dev.vality.proxy.mocketbank.service.mpi.model.ValidatePaResResponse;
import dev.vality.proxy.mocketbank.service.mpi20.model.SessionState;
import dev.vality.proxy.mocketbank.service.mpi20.processor.Mpi20Processor;
import dev.vality.proxy.mocketbank.utils.CallbackResponseWithTemplateCreator;
import dev.vality.proxy.mocketbank.utils.Converter;
import dev.vality.proxy.mocketbank.utils.CreatorUtils;
import dev.vality.proxy.mocketbank.utils.ErrorBuilder;
import dev.vality.proxy.mocketbank.utils.model.Card;
import dev.vality.proxy.mocketbank.utils.model.CardAction;
import dev.vality.proxy.mocketbank.utils.model.CardUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCallbackHandler {

    private final CdsStorageClient cds;
    private final MpiApi mpiApi;
    private final ErrorMapping errorMapping;
    private final List<Card> cardList;
    private final ObjectMapper objectMapper;
    private final Mpi20Processor mpi20Processor;
    private final CallbackResponseWithTemplateCreator callbackResponseCreator;

    public PaymentCallbackResult handler(ByteBuffer byteBuffer, PaymentContext context) {
        CardDataProxyModel cardData;
        try {
            cardData = cds.getCardData(context);
        } catch (CdsStorageExpDateException ex) {
            return ErrorBuilder.prepareCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, ex.getMessage());
        }

        Card card = CardUtils.extractCardByPan(cardList, cardData.getPan())
                .orElseThrow(() -> new IllegalStateException("Card must be set"));

        if (CardAction.isCardEnrolled(card)) {
            return processEnrolled(byteBuffer, context, cardData);
        } else if (CardAction.isCardEnrolled20(card)) {
            CardAction cardAction = CardAction.findByValue(card.getAction());
            return processEnrolled20(context, CardUtils.getHttpMethodByCardAction(cardAction));
        } else {
            throw new RuntimeException("Card should be enrolled");
        }
    }

    @SneakyThrows
    private PaymentCallbackResult processEnrolled20(PaymentContext context, String formMethod) {
        SessionState contextSessionState = objectMapper.readValue(context.getSession().getState(), SessionState.class);
        switch (contextSessionState.getState()) {
            case PREPARE:
                PaymentCallbackProxyResult authCallbackProxyResult = mpi20Processor.processAuth(context);
                SessionState authSessionState = objectMapper.readValue(
                        authCallbackProxyResult.getNextState(),
                        SessionState.class);
                return createCallbackResult(
                        callbackResponseCreator.createCallbackResponseWithForm(
                                authSessionState.getOptions(),
                                formMethod,
                                context
                        ),
                        authCallbackProxyResult);
            case AUTH:
                return createCallbackResult(new byte[]{}, mpi20Processor.processResult(context));
            default:
                throw new IllegalStateException("Illegal state");
        }
    }


    private PaymentCallbackResult processEnrolled(
            ByteBuffer byteBuffer,
            PaymentContext context,
            CardDataProxyModel cardData) {
        HashMap<String, String> parameters = Converter.mergeParams(byteBuffer, context.getSession().getState());
        ValidatePaResResponse validatePaResResponse = mpiApi.validatePaRes(cardData, parameters);
        if (TransactionStatus.isAuthenticationSuccessful(validatePaResResponse.getTransactionStatus())) {
            TransactionInfo transactionInfo = CreatorUtils.createDefaultTransactionInfo(context);
            PaymentCallbackProxyResult proxyResult = createCallbackProxyResult(
                    createFinishIntentSuccess(), PaymentState.CAPTURED.getBytes(), transactionInfo
            );
            return createCallbackResult("".getBytes(), proxyResult);
        }

        CardAction action = CardUtils.extractActionFromCard(cardList, cardData);
        return ErrorBuilder.prepareCallbackError(errorMapping, Error.DEFAULT_ERROR_CODE, action);
    }

}
