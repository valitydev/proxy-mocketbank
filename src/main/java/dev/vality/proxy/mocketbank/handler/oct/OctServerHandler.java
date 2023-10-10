package dev.vality.proxy.mocketbank.handler.oct;

import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.adapter.common.damsel.DomainPackageCreators;
import dev.vality.adapter.common.damsel.WithdrawalsProviderAdapterPackageCreators;
import dev.vality.adapter.common.mapper.ErrorMapping;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.msgpack.Value;
import dev.vality.damsel.withdrawals.provider_adapter.*;
import dev.vality.proxy.mocketbank.service.CdsService;
import dev.vality.proxy.mocketbank.service.oct.verification.CurrencyVerification;
import dev.vality.proxy.mocketbank.utils.ErrorBuilder;
import dev.vality.proxy.mocketbank.utils.payout.CardPayout;
import dev.vality.proxy.mocketbank.utils.payout.CardPayoutAction;
import dev.vality.proxy.mocketbank.utils.payout.PayoutUtils;
import dev.vality.proxy.mocketbank.validator.WithdrawalValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OctServerHandler implements AdapterSrv.Iface {

    private final CdsService cdsService;
    private final ErrorMapping errorMapping;
    private final List<CardPayout> cardPayoutList;
    private final WithdrawalValidator withdrawalValidator;

    @Override
    public ProcessResult processWithdrawal(
            Withdrawal withdrawal,
            Value state,
            Map<String, String> options) {

        if (withdrawal.getDestination().isSetBankCard()) {
            CardDataProxyModel cardData = cdsService.getCardData(withdrawal);
            log.info("cardPayoutList {}", cardPayoutList);
            Optional<CardPayout> cardPayout = PayoutUtils.extractCardPayoutByPan(cardPayoutList, cardData.getPan());
            if (cardPayout.isPresent()) {
                log.info("Found card payout with action {}", cardPayout.get().getAction());
                if (CardPayoutAction.isCardFailed(cardPayout.get())) {
                    CardPayoutAction action = CardPayoutAction.findByValue(cardPayout.get().getAction());
                    log.info("Failed card payout with action {}", action);
                    return ErrorBuilder.prepareWithdrawalError(errorMapping, action);
                }
            }
        }

        TransactionInfo transactionInfo =
                DomainPackageCreators.createTransactionInfo(withdrawal.getId(), Collections.emptyMap());
        Intent intent = WithdrawalsProviderAdapterPackageCreators.createFinishIntentSuccess(transactionInfo);
        return WithdrawalsProviderAdapterPackageCreators.createProcessResult(intent);
    }

    @Override
    public Quote getQuote(GetQuoteParams getQuoteParams, Map<String, String> options) {
        withdrawalValidator.validate(getQuoteParams, options);

        Cash cashFrom = new Cash().setCurrency(getQuoteParams.getCurrencyFrom());
        Cash cashTo = new Cash().setCurrency(getQuoteParams.getCurrencyTo());
        if (CurrencyVerification.isCryptoCurrency(getQuoteParams.getCurrencyTo())) {
            cashFrom.setAmount(getQuoteParams.getExchangeCash().getAmount());
            cashTo.setAmount(getQuoteParams.getExchangeCash().getAmount() * 2);
        } else {
            cashFrom.setAmount(getQuoteParams.getExchangeCash().getAmount() * 2);
            cashTo.setAmount(getQuoteParams.getExchangeCash().getAmount());
        }

        Value quoteData = new Value();
        quoteData.setStr(getQuoteParams.getIdempotencyId());
        return new Quote()
                .setCashTo(cashTo)
                .setCashFrom(cashFrom)
                .setQuoteData(quoteData)
                .setCreatedAt(getCurrentDateTimeByPattern(Instant.EPOCH.toEpochMilli()))
                .setExpiresOn(getCurrentDateTimeByPattern(Instant.EPOCH
                        .plus(15, ChronoUnit.MINUTES)
                        .toEpochMilli())
                );
    }

    @Override
    public CallbackResult handleCallback(
            Callback callback,
            Withdrawal withdrawal,
            Value value,
            Map<String, String> map) throws TException {
        throw new TException("Not supported method handleCallback");
    }

    private static String getCurrentDateTimeByPattern(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).toString();
    }

}
