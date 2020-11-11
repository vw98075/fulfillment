package com.vw.example.kitchen.fulfillment;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vw.example.kitchen.fulfillment.data.Type;
import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrder;
import com.vw.example.kitchen.fulfillment.data.domain.ReceivedOrder;
import com.vw.example.kitchen.fulfillment.data.domain.ShelfRack;
import com.vw.example.kitchen.fulfillment.serivce.NewOrderHandler;
import com.vw.example.kitchen.fulfillment.serivce.OrderReadyHandler;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class AddAndOrRemoveOrderTest {

    static final List<FulfilledOrder> hotOrders = new LinkedList<>();
    static final List<FulfilledOrder> coldOrders = new LinkedList<>();
    static final List<FulfilledOrder> frozenOrders = new LinkedList<>();

    /*
        Shelf size:
            hot, cold, frozen - 2
            overflow - 3
     */
    final static int regularShelfSize = 2;
    final static int overflowShelfSize = 3;

    final Map<Type, ShelfRack> shelfMap = new EnumMap<>(Type.class);

    NewOrderHandler newOrderHandler;

    OrderReadyHandler orderReadyHandler;

    @BeforeAll
    static void loadData() {
//        System.out.println(":::: --- start init() ---");

        int orderReceiveRate = 2;
        try {
            ObjectMapper mapper  = new ObjectMapper();
            List<ReceivedOrder> orderList = mapper.readValue(
                    new InputStreamReader(AddAndOrRemoveOrderTest.class.getResourceAsStream("/orders.json")),
                    new TypeReference<>() {
                    });

            int mSecond = 1000/orderReceiveRate;
            for(ReceivedOrder order : orderList){
                TimeUnit.MILLISECONDS.sleep(mSecond);
                switch (order.getTemp()) {
                    case HOT -> hotOrders.add(new FulfilledOrder(order, LocalDateTime.now()));
                    case COLD -> coldOrders.add(new FulfilledOrder(order, LocalDateTime.now()));
                    case FROZEN -> frozenOrders.add(new FulfilledOrder(order, LocalDateTime.now()));
                    default -> System.out.println("Error in input data temperature");
                }
            }

        } catch (Exception ex) {
            System.out.println(Arrays.toString(ex.getStackTrace()));
        }

//        System.out.println("--- end init() ---");
    }

    @BeforeEach
    void initMap(){
        shelfMap.put(Type.COLD, new ShelfRack(Type.COLD, 1, 2));
        shelfMap.put(Type.FROZEN, new ShelfRack(Type.FROZEN, 1, 2));
        shelfMap.put(Type.HOT, new ShelfRack(Type.HOT, 1, 2));
        shelfMap.put(Type.OVERFLOW, new ShelfRack(Type.OVERFLOW, 1, 3));

        newOrderHandler = new NewOrderHandler(shelfMap);
        orderReadyHandler = new OrderReadyHandler(shelfMap);
    }
    
    @Test
    void addTwoHotOrders(){
        hotOrders
                .stream()
                .limit(2)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.HOT));
        assertEquals ( newOrderHandler.shelfRackSize(Type.HOT), 2);
        System.out.println("---------- addTwoHotOrders(): expecting 2 hot orders in a hot shelf ----------");
        shelfMap
                .get(Type.HOT)
                .getSnapShotOfFulfilledOrders()
//                .forEach(order ->  assertEquals ( Type.HOT, order.getShelfType()));
                .forEach(System.out::println);
        assertEquals ( newOrderHandler.shelfRackSize(Type.COLD), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.FROZEN), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.OVERFLOW), 0);
    }

    @Test
    void addThreeHotOrders(){
        hotOrders
                .stream()
                .limit(3)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.HOT));
        assertEquals ( newOrderHandler.shelfRackSize(Type.HOT), 2);
        assertEquals ( newOrderHandler.shelfRackSize(Type.COLD), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.FROZEN), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.OVERFLOW), 1);
    }

    @Test
    void addThreeHotAndFourColdOrders(){
        hotOrders
                .stream()
                .limit(3)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.HOT));
        coldOrders
                .stream()
                .limit(4)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.COLD));
        assertEquals ( newOrderHandler.shelfRackSize(Type.HOT),2);
        assertEquals ( newOrderHandler.shelfRackSize(Type.COLD) , 2);
        assertEquals ( newOrderHandler.shelfRackSize(Type.FROZEN), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.OVERFLOW), 3);
        System.out.println("---------- addThreeHotAndFourColdOrders(): expecting 1 hot order and 2 cold orders in an overflow shelf ----------");
        shelfMap
                .get(Type.OVERFLOW)
                .getSnapShotOfFulfilledOrders()
                .forEach(System.out::println);
    }

    @Test
    void addThreeHotAndFiveColdOrders(){
        hotOrders
                .stream()
                .limit(3)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.HOT));
        coldOrders
                .stream()
                .limit(5)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.COLD));
        assertEquals ( newOrderHandler.shelfRackSize(Type.HOT),2);
        assertEquals ( newOrderHandler.shelfRackSize(Type.COLD) , 2);
        assertEquals ( newOrderHandler.shelfRackSize(Type.FROZEN), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.OVERFLOW), 3);
        System.out.println("---------- addThreeHotAndFiveColdOrders: an order is removed from collection of 1 hot order and 2 cold orders and add 1 cold order in an overflow shelf ----------");
        shelfMap
                .get(Type.OVERFLOW)
                .getSnapShotOfFulfilledOrders()
                .forEach(System.out::println);
    }

    @Test
    void removeOneHotOrderAndAddOneMoreHotOrder(){
        hotOrders
                .stream()
                .limit(3)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.HOT));
        coldOrders
                .stream()
                .limit(5)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.COLD));

        newOrderHandler.removeOneOrderFromShelfRack(Type.HOT);
        assertEquals ( newOrderHandler.shelfRackSize(Type.HOT),  1);

        newOrderHandler.addNewOrderToShelf(hotOrders.get(3), Type.HOT);
        assertEquals( newOrderHandler.shelfRackSize(Type.HOT), 2);
    }

    @Test
    void removeOneHotOrderAndAddOneMoreColdOrder(){
        hotOrders
                .stream()
                .limit(3)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.HOT));
        coldOrders
                .stream()
                .limit(4)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.COLD));

        newOrderHandler.removeOneOrderFromShelfRack(Type.HOT);
        assertEquals ( newOrderHandler.shelfRackSize(Type.HOT), 1);

        newOrderHandler.addNewOrderToShelf(coldOrders.get(4), Type.COLD);
        assertEquals ( newOrderHandler.shelfRackSize(Type.HOT), 2);
        assertEquals ( newOrderHandler.shelfRackSize(Type.COLD), 2);
        assertEquals ( newOrderHandler.shelfRackSize(Type.FROZEN), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.OVERFLOW), 3);
        System.out.println("---------- removeOneHotOrderAndAddOneMoreColdOrder: expecting 3 cold orders in an overflow shelf ----------");
        shelfMap
                .get(Type.OVERFLOW)
                .getSnapShotOfFulfilledOrders()
                .forEach(System.out::println);
    }

    /*****************  **********************/
    @Test
    void addTwoHotOrdersAndRemoveOne(){
        hotOrders
                .stream()
                .limit(2)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.HOT));
        orderReadyHandler.removeOrderForDelivery(hotOrders.get(1));
        assertEquals ( newOrderHandler.shelfRackSize(Type.HOT), 1);
        assertEquals ( newOrderHandler.shelfRackSize(Type.COLD), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.FROZEN), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.OVERFLOW), 0);
    }

    @Test
    void addThreeHotOrdersAndRemoveTheLastOne(){
        hotOrders
                .stream()
                .limit(3)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.HOT));
        orderReadyHandler.removeOrderForDelivery(hotOrders.get(2));
        assertEquals ( newOrderHandler.shelfRackSize(Type.HOT), 2);
        assertEquals ( newOrderHandler.shelfRackSize(Type.COLD), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.FROZEN), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.OVERFLOW), 0);
    }

    @Test
    void addThreeHotOrdersAndRemoveTheSecondOne(){
        hotOrders
                .stream()
                .limit(3)
                .forEach( order -> newOrderHandler.addNewOrderToShelf(order, Type.HOT));
        orderReadyHandler.removeOrderForDelivery(hotOrders.get(0));
        assertEquals ( newOrderHandler.shelfRackSize(Type.HOT), 1);
        assertEquals ( newOrderHandler.shelfRackSize(Type.COLD), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.FROZEN), 0);
        assertEquals ( newOrderHandler.shelfRackSize(Type.OVERFLOW), 1);
    }
}