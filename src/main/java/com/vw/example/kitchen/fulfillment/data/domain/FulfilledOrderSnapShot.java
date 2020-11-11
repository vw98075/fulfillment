package com.vw.example.kitchen.fulfillment.data.domain;

import com.vw.example.kitchen.fulfillment.data.Type;
import lombok.*;

@Getter
@EqualsAndHashCode(callSuper = true)
public final class FulfilledOrderSnapShot extends FulfilledOrder {

    private final Type shelfType;

    private final float expirationValue;

    public FulfilledOrderSnapShot(Type type, FulfilledOrder order, float value){
        super(order.getId(), order.getName(), order.getTemp(), order.shelfLife, order.decayRate, order.getFulfilledTime());
        this.shelfType = type;
        this.expirationValue = value;
    }

    @Override
    public String toString() {
        return "FulfilledOrder{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", temp=" + getTemp() +
                ", shelfLife=" + shelfLife +
                ", decayRate=" + decayRate +
                ", fulfilledTime=" +getFulfilledTime() +
                ", shelfType=" + shelfType +
                ", expirationValue=" + expirationValue +
                '}';
    }
}
