package dev.vality.proxy.mocketbank.utils;

import dev.vality.damsel.user_interaction.UserInteraction;
import dev.vality.proxy.mocketbank.utils.model.CardAction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createGetUserInteraction;
import static dev.vality.adapter.common.damsel.ProxyProviderPackageCreators.createPostUserInteraction;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserInteractionUtils {

    public static UserInteraction getUserInteraction(String url,
                                                     Map<String, String> params,
                                                     CardAction action) {
        if (CardAction.isGetAcsCard(action)) {
            return createGetUserInteraction(UrlUtils.getCallbackUrl(url, params));
        }
        return createPostUserInteraction(url, params);
    }
}
