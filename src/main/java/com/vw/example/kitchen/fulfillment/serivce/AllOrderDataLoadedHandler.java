package com.vw.example.kitchen.fulfillment.serivce;

import com.vw.example.kitchen.fulfillment.data.AllOrderDataLoadedEvent;
import com.vw.example.kitchen.fulfillment.data.domain.ShelfRack;
import com.vw.example.kitchen.fulfillment.data.Type;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class AllOrderDataLoadedHandler {

    @Autowired
    private final Map<Type, ShelfRack> shelfMap;

    @Autowired
    ApplicationContext ctx;

    public AllOrderDataLoadedHandler(Map<Type, ShelfRack> shelfMap, ApplicationContext ctx) {
        this.shelfMap = shelfMap;
        this.ctx = ctx;
    }

    @EventListener
    public void onNewOrderEvent(@NonNull AllOrderDataLoadedEvent event) throws Exception{
        log.info("Receive AllOrderDataLoadedEvent");
        int numberOfEntries = 0;
        do{
            TimeUnit.MILLISECONDS.sleep(9000); // delay 9 second
            numberOfEntries = shelfMap
                    .values()
                    .stream()
                    .map(rack -> rack.getTotalNumberOfOrders())
                    .reduce(0, Integer::sum);
        }while(numberOfEntries != 0);

        log.info("All order data has been processed. This application is going to be shut down in 5 second.");
        TimeUnit.MILLISECONDS.sleep(5000);
        SpringApplication.exit(ctx, () -> 0);
    }
}
