package com.rbkmoney.proxy.mocketbank.service;

import com.rbkmoney.damsel.withdrawals.provider_adapter.Withdrawal;
import com.rbkmoney.java.cds.utils.model.CardDataProxyModel;

public interface CdsService {

    CardDataProxyModel getCardData(Withdrawal withdrawal);
}
