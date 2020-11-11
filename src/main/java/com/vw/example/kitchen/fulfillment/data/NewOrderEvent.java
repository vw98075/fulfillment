package com.vw.example.kitchen.fulfillment.data;

import com.vw.example.kitchen.fulfillment.data.domain.ReceivedOrder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public final class NewOrderEvent extends ApplicationEvent {

    private final ReceivedOrder order;

    /**
     *
     * @param source
     * @param o
     */
    public NewOrderEvent(Object source, ReceivedOrder o){
        super(source);
        this.order = o;
    }
}
