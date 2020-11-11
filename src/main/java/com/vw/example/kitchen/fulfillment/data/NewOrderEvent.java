package com.vw.example.kitchen.fulfillment.data;

import com.vw.example.kitchen.fulfillment.data.domain.ReceivedOrder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public final class NewOrderEvent {

    private final ReceivedOrder order;

    /**
     *
     * @param o
     */
    public NewOrderEvent(ReceivedOrder o){
        this.order = o;
    }
}
