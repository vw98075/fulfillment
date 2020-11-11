package com.vw.example.kitchen.fulfillment.data;

import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderReadyEvent {

    private final FulfilledOrder order;

    public OrderReadyEvent(FulfilledOrder o){
        this.order = o;
    }
}
