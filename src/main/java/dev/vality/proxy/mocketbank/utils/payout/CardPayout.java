package dev.vality.proxy.mocketbank.utils.payout;

import dev.vality.proxy.mocketbank.utils.model.CreditCardUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CardPayout {
    private final String pan;
    private final String action;
    private final String paymentSystem;

    @Override
    public String toString() {
        return "CardPayout{" +
                "pan='" + CreditCardUtils.maskNumber(pan) + '\'' +
                ", action='" + action + '\'' +
                ", paymentSystem='" + paymentSystem + '\'' +
                '}';
    }
}
