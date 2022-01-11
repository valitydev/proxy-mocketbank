package com.rbkmoney.proxy.mocketbank.utils;

import com.rbkmoney.damsel.user_interaction.UserInteraction;
import com.rbkmoney.proxy.mocketbank.utils.model.CardAction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createGetUserInteraction;
import static com.rbkmoney.java.damsel.utils.creators.ProxyProviderPackageCreators.createPostUserInteraction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserInteractionUtils {

    public static UserInteraction prepareUserInteraction(String url,
                                                   Map<String, String> params,
                                                   CardAction action) {
        if (CardAction.isGetAcsCard(action)) {
            return createGetUserInteraction(UrlUtils.getCallbackUrl(url, params));
        }
        return createPostUserInteraction(url, params);
    }
}
