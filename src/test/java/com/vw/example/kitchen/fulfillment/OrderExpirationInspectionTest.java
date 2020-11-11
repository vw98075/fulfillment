package com.vw.example.kitchen.fulfillment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vw.example.kitchen.fulfillment.data.domain.ShelfRack;
import com.vw.example.kitchen.fulfillment.data.Type;
import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrder;
import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrderSnapShot;
import com.vw.example.kitchen.fulfillment.data.domain.ReceivedOrder;
import com.vw.example.kitchen.fulfillment.serivce.NewOrderHandler;
import com.vw.example.kitchen.fulfillment.serivce.OrderExpirationInspectionTasks;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderExpirationInspectionTest {

    final Map<Type, ShelfRack> shelfMap = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(Type.COLD, new ShelfRack(Type.HOT, 1, 2)),
            new AbstractMap.SimpleEntry<>(Type.FROZEN, new ShelfRack(Type.COLD, 1, 2)),
            new AbstractMap.SimpleEntry<>(Type.HOT, new ShelfRack(Type.FROZEN, 1, 2)),
            new AbstractMap.SimpleEntry<>(Type.OVERFLOW, new ShelfRack(Type.OVERFLOW, 1, 3))
    );

    final NewOrderHandler newOrderHandler = new NewOrderHandler(shelfMap);

    final OrderExpirationInspectionTasks tasks = new OrderExpirationInspectionTasks(shelfMap);

    @BeforeAll
    public void init(){
        System.out.println(":::: --- start init() ---");
        int orderReceiveRate = 2;
        try {
            ObjectMapper mapper  = new ObjectMapper();
            List<ReceivedOrder> orderList = mapper.readValue(
                    new InputStreamReader(getClass().getResourceAsStream("/orders.json")),
                    new TypeReference<>() {
                    });

            int mSecond = 1000/ orderReceiveRate;
            for(ReceivedOrder order : orderList){
                TimeUnit.MILLISECONDS.sleep(mSecond);
                switch (order.getTemp()) {
                    case HOT -> newOrderHandler.addNewOrderToShelf(new FulfilledOrder(order, LocalDateTime.now()), Type.HOT);
                    case COLD -> newOrderHandler.addNewOrderToShelf(new FulfilledOrder(order, LocalDateTime.now()), Type.COLD);
                    case FROZEN -> newOrderHandler.addNewOrderToShelf(new FulfilledOrder(order, LocalDateTime.now()), Type.FROZEN);
                    default -> System.err.println("Temperature data error: " + order.getTemp());
                }
            }
        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }
        System.out.println("--- end init() ---");
    }

    @Test
    public void stateTest() throws Exception{

        Set<FulfilledOrderSnapShot> removedOrders;
        int count = 0;
        Set<Type> keys = shelfMap.keySet();
        while(count < 9){
            TimeUnit.MILLISECONDS.sleep(5000);
            removedOrders = tasks.removeExpiredOrders(keys);
            for (FulfilledOrderSnapShot removedOrder : removedOrders) {
                assertTrue(removedOrder.getExpirationValue() < 0.0F);
            }
            count++;
        }
    }
}
