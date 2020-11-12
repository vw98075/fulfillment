package com.vw.example.kitchen.fulfillment.serivce;

import com.vw.example.kitchen.fulfillment.data.OrderReadyEvent;
import com.vw.example.kitchen.fulfillment.data.domain.ShelfRack;
import com.vw.example.kitchen.fulfillment.data.Type;
import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrder;
import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrderSnapShot;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OrderReadyHandler {

    static final int MIN = 2;
    static final int MAX = 6;
    private final Map<Type, ShelfRack> shelfMap;

    public OrderReadyHandler(Map<Type, ShelfRack> shelfMap) {
        this.shelfMap = shelfMap;
    }


    @Async
    @EventListener
    public void onNewOrderEvent(@NonNull OrderReadyEvent event) throws Exception{

        FulfilledOrder order = event.getOrder();
        log.info("Receive a delivery notification for " + order.toString());

        int d = ThreadLocalRandom.current().nextInt(MIN, MAX + 1);
        TimeUnit.MILLISECONDS.sleep(d * 1000);

        /*
            find the order and remove from a shelf for delivery
         */
        removeOrderForDelivery(order);
    }

    public void removeOrderForDelivery(final FulfilledOrder order) {
        ShelfRack rack ;
        boolean isSuccessful;
        switch(order.getTemp()){
            case HOT -> {
                    rack = shelfMap.get(Type.HOT);
                    isSuccessful = rack.removeOrder(order);
            }
            case COLD -> {
                rack = shelfMap.get(Type.COLD);
                isSuccessful = rack.removeOrder(order);
            }
            case FROZEN -> {
                rack = shelfMap.get(Type.FROZEN);
                isSuccessful = rack.removeOrder(order);
            }
            default -> throw new IllegalStateException("Unexpected value: " + order.getTemp());
        }
        if(!isSuccessful){
            ShelfRack overflowRack = shelfMap.get(Type.OVERFLOW);
            isSuccessful = overflowRack.removeOrder(order);
            if(isSuccessful){
                log.debug("Order removed from overflow shelf, " + order.toString() + " removed from overflow shelf");
            }else {
                log.debug(">>> Order, " + order.toString() + " doesn't get removed <<<");
            }
        }else{
            log.debug("Order removed, " + order.toString());
        }

        printData();
    }

    private void printData(){

        Set<Type> types = shelfMap.keySet();
        for (Type t : types) {
            ShelfRack shelfRack = shelfMap.get(t);
            shelfRack.printShapshuts();
        }
    }
}
