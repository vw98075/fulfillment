package com.vw.example.kitchen.fulfillment.serivce;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vw.example.kitchen.fulfillment.data.AllOrderDataLoadedEvent;
import com.vw.example.kitchen.fulfillment.data.NewOrderEvent;
import com.vw.example.kitchen.fulfillment.data.domain.ReceivedOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Load data from file to this application
 */
@Component
@Profile("prod")
@Slf4j
public class LoadDataService implements CommandLineRunner {

    @Value("${order.receive.rate_per_second:2}")
    private int orderReceiveRate;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public void run(String... args) throws Exception {
        try {
            ObjectMapper mapper  = new ObjectMapper();
            List<ReceivedOrder> orderList = mapper.readValue(
                    new InputStreamReader(getClass().getResourceAsStream("/orders.json")),
                    new TypeReference<>() {
                    });

            int mSecond = 1000/ orderReceiveRate;
            for(ReceivedOrder order : orderList){
                TimeUnit.MILLISECONDS.sleep(mSecond);
                publisher.publishEvent(new NewOrderEvent(this, order));
            }
        } catch (Exception ex) {
            log.error(Arrays.toString(ex.getStackTrace()));
        }
        publisher.publishEvent(new AllOrderDataLoadedEvent());
    }
}
