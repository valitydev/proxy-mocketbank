package dev.vality.proxy.mocketbank.utils.model;

import dev.vality.adapter.common.cds.model.CardDataProxyModel;
import dev.vality.proxy.mocketbank.exception.CardException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CardUtils {

    public static Optional<Card> extractCardByPan(List<Card> cardList, String pan) {
        return cardList.stream().filter(card -> card.getPan().equals(pan)).findFirst();
    }

    public static CardAction extractActionFromCard(List<Card> cardList, CardDataProxyModel cardData) {
        Optional<Card> card = CardUtils.extractCardByPan(cardList, cardData.getPan());
        if (!card.isPresent()) {
            throw new CardException("Can't extract action from card");
        }
        CardAction action = CardAction.findByValue(card.get().getAction());
        return CardAction.isMpiCardFailed(action) ? action : CardAction.UNKNOWN_FAILURE;
    }

    public static String getHttpMethodByCardAction(CardAction action) {
        return CardAction.isGetAcsCard(action) ? HttpMethod.GET.name() : HttpMethod.POST.name();
    }
}
