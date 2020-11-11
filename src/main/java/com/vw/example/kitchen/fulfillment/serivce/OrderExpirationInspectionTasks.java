package com.vw.example.kitchen.fulfillment.serivce;

import com.vw.example.kitchen.fulfillment.data.Type;
import com.vw.example.kitchen.fulfillment.data.ShelfRack;
import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrderSnapShot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Profile("prod")
@Slf4j
public class OrderExpirationInspectionTasks {

    @Autowired
    private final Map<Type, ShelfRack> shelfMap;

    public OrderExpirationInspectionTasks(Map<Type, ShelfRack> shelfMap) {
        this.shelfMap = shelfMap;
    }

    @Scheduled(fixedRateString = "${order.expiration.check.rate:5000}")
    public void inspect() {

        Set<FulfilledOrderSnapShot> removedOrders;
        Set<Type> keys = shelfMap.keySet();
        removedOrders = removeExpiredOrders(keys);

        log.debug("--- Removed orders due to expiration ---");
        removedOrders.forEach(order -> log.info(order.toString()));
        log.debug("--- ----------------------------------- ---");
    }

    // public access for test
    public Set<FulfilledOrderSnapShot> removeExpiredOrders(Set<Type> types){
        Set<FulfilledOrderSnapShot> removedOrders = new HashSet<>();
        for(Type type : types){
            ShelfRack shelfRack = shelfMap.get(type);
            removedOrders.addAll(shelfRack.removeExpiredOrders());
        }
        return removedOrders;
    }
}
