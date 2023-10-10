package dev.vality.proxy.mocketbank.service;

import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.damsel.withdrawals.provider_adapter.Withdrawal;

public interface CdsService {

    CardDataProxyModel getCardData(Withdrawal withdrawal);
}
