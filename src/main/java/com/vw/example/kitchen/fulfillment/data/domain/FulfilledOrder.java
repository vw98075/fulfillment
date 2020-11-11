package com.vw.example.kitchen.fulfillment.data.domain;

import com.vw.example.kitchen.fulfillment.data.Type;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(callSuper = true)
public class FulfilledOrder extends ReceivedOrder {

    private final LocalDateTime fulfilledTime;

    public FulfilledOrder(ReceivedOrder order, LocalDateTime now){
        super(order.getId(), order.getName(), order.getTemp(), order.shelfLife, order.decayRate);
        this.fulfilledTime = now;
    }

    protected FulfilledOrder(String id, String name, Temperature temp, int shelfLife, float decayRate, LocalDateTime fulfilledTime) {
        super(id, name, temp, shelfLife, decayRate);
        this.fulfilledTime = fulfilledTime;
    }

    public boolean isStillGood(float value){
        return value > 0.0F;
    }

    public float calculateExpirationValue(Type type){
        long orderAge = Duration.between(this.fulfilledTime, LocalDateTime.now()).toSeconds();
        return 1.0F - (orderAge + this.decayRate * orderAge * (Type.OVERFLOW.equals(type) ? 2.0F : 1.0F))/this.shelfLife;
    }

    @Override
    public String toString() {
        return "FulfilledOrder{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", temp=" + getTemp() +
                ", shelfLife=" + shelfLife +
                ", decayRate=" + decayRate +
                ", fulfilledTime=" + fulfilledTime +
                '}';
    }
}
