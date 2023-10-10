package dev.vality.proxy.mocketbank.utils.dw;

import dev.vality.damsel.domain.DigitalWallet;
import dev.vality.damsel.proxy_provider.Cash;
import dev.vality.proxy.mocketbank.configuration.properties.AdapterMockBankProperties;
import dev.vality.proxy.mocketbank.utils.UrlUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DwCreators {

    public static HashMap<String, String> createDigitalWalletParams(
            String invoiceId,
            Cash cash,
            DigitalWallet digitalWallet,
            AdapterMockBankProperties mockBankProperties
    ) {
        HashMap<String, String> params = new HashMap<>();
        params.put("shop", "test");
        params.put("bill_id", invoiceId);

        String url = UrlUtils.getCallbackUrl(
                mockBankProperties.getCallbackUrl(),
                mockBankProperties.getPathDigitalWalletCallbackUrl()
        );
        params.put("success_url", "{termination_uri}");
        params.put("fail_url", "{termination_uri}");
        params.put("callback_url", url);

        String amount = String.valueOf(cash.getAmount());
        params.put("amount", amount);

        String user = (digitalWallet == null) ? "" : digitalWallet.getId();
        params.put("user", "tel:" + user);
        return params;
    }

}
