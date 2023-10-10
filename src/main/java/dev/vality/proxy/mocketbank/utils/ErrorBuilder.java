package dev.vality.proxy.mocketbank.utils;

import dev.vality.adapter.common.damsel.WithdrawalsProviderAdapterPackageCreators;
import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.damsel.domain.Failure;
import dev.vality.damsel.proxy_provider.PaymentCallbackResult;
import dev.vality.damsel.proxy_provider.PaymentProxyResult;
import dev.vality.damsel.proxy_provider.RecurrentTokenCallbackResult;
import dev.vality.damsel.proxy_provider.RecurrentTokenProxyResult;
import dev.vality.proxy.mocketbank.utils.model.CardAction;
import dev.vality.proxy.mocketbank.utils.payout.CardPayoutAction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorBuilder {

    public static PaymentProxyResult prepareError(ErrorMapping errorMapping, String code, String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return createProxyResultFailure(failure);
    }

    public static PaymentProxyResult prepareError(ErrorMapping errorMapping, CardAction action) {
        return prepareError(errorMapping, action.getAction());
    }

    public static PaymentProxyResult prepareError(ErrorMapping errorMapping, String code) {
        return prepareError(errorMapping, code, code);
    }

    public static RecurrentTokenProxyResult prepareRecurrentTokenError(ErrorMapping errorMapping, String code) {
        return prepareRecurrentTokenError(errorMapping, code, code);
    }

    public static RecurrentTokenProxyResult prepareRecurrentTokenError(ErrorMapping errorMapping, CardAction action) {
        return prepareRecurrentTokenError(errorMapping, action.getAction());
    }

    public static RecurrentTokenProxyResult prepareRecurrentTokenError(
            ErrorMapping errorMapping,
            String code,
            String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return createRecurrentTokenProxyResultFailure(failure);
    }

    public static PaymentCallbackResult prepareCallbackError(ErrorMapping errorMapping, String code, String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return createCallbackResultFailure(failure);
    }

    public static PaymentCallbackResult prepareCallbackError(
            ErrorMapping errorMapping,
            String code,
            CardAction action) {
        return prepareCallbackError(errorMapping, code, action.getAction());
    }

    public static RecurrentTokenCallbackResult prepareRecurrentCallbackError(
            ErrorMapping errorMapping,
            String code,
            CardAction action) {
        return prepareRecurrentCallbackError(errorMapping, code, action.getAction());
    }

    public static RecurrentTokenCallbackResult prepareRecurrentCallbackError(
            ErrorMapping errorMapping,
            String code,
            String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return createRecurrentTokenCallbackResultFailure(failure);
    }

    public static dev.vality.damsel.withdrawals.provider_adapter.ProcessResult prepareWithdrawalError(
            ErrorMapping errorMapping,
            String code,
            String message) {
        Failure failure = errorMapping.mapFailure(code, message);
        return WithdrawalsProviderAdapterPackageCreators.createProcessResultFailure(failure);
    }

    public static dev.vality.damsel.withdrawals.provider_adapter.ProcessResult prepareWithdrawalError(
            ErrorMapping errorMapping,
            String code,
            CardPayoutAction action) {
        return prepareWithdrawalError(errorMapping, code, action.getAction());
    }

    public static dev.vality.damsel.withdrawals.provider_adapter.ProcessResult prepareWithdrawalError(
            ErrorMapping errorMapping,
            CardPayoutAction action) {
        return prepareWithdrawalError(errorMapping, action.getAction(), action.getAction());
    }

}
