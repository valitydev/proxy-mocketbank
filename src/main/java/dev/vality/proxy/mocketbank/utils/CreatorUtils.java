package dev.vality.proxy.mocketbank.utils;

import dev.vality.adapter.common.damsel.ProxyProviderPackageCreators;
import dev.vality.damsel.domain.TransactionInfo;
import dev.vality.damsel.proxy_provider.PaymentContext;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;

import static dev.vality.adapter.common.damsel.DomainPackageCreators.createTransactionInfo;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CreatorUtils {

    public static TransactionInfo createDefaultTransactionInfo(PaymentContext context) {
        return createTransactionInfo(
                ProxyProviderPackageCreators.createInvoiceWithPayment(context.getPaymentInfo()),
                Collections.emptyMap());
    }

}
