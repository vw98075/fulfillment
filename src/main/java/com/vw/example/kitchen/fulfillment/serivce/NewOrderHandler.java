package com.vw.example.kitchen.fulfillment.serivce;

import com.vw.example.kitchen.fulfillment.data.*;
import com.vw.example.kitchen.fulfillment.data.domain.*;
import com.vw.example.kitchen.fulfillment.data.Temperature;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class NewOrderHandler {

    private final Map<Type, ShelfRack> shelfMap;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    public NewOrderHandler(Map<Type, ShelfRack> shelfMap) {
        this.shelfMap = shelfMap;
    }

    @EventListener
    @Async
    public void onNewOrderEvent(@NonNull NewOrderEvent event) {

        ReceivedOrder receivedOrder = event.getOrder();
        log.info("Receive a new order notification for " + receivedOrder.toString());

        FulfilledOrder newOrder = new FulfilledOrder(receivedOrder, LocalDateTime.now()); // in the real world, there is a time for food preparation
        Temperature temp = receivedOrder.getTemp();

        switch (temp) {
            case HOT -> addNewOrderToShelf(newOrder, Type.HOT);
            case COLD -> addNewOrderToShelf(newOrder, Type.COLD);
            case FROZEN -> addNewOrderToShelf(newOrder, Type.FROZEN);
            default -> log.error("Temperature data error: " + temp);
        }

        publisher.publishEvent(new OrderReadyEvent(newOrder));
    }

    /**
     * add a new order to the shelf (rack) with the given type
     *
     * @param newOrder
     * @param type
     */
    public void addNewOrderToShelf(FulfilledOrder newOrder, Type type) {

        log.info("Try to add an order, " + newOrder.toString() + " to " + type + " shelf");

        ShelfRack group = shelfMap.get(type);
        boolean isAdded = group.addOrder(newOrder);
        if (!isAdded) {
            /*
                The new order isn't added to its shelf.
             */
            ShelfRack overflowGroup = shelfMap.get(Type.OVERFLOW);
            isAdded = overflowGroup.addOrder(newOrder);
            if (!isAdded) {
                /*
                 The new order isn't added to the overflow shelf.
                 */
                Set<Temperature> set = this.findOrderTemperaturesWithRoomOtherThanTemperature(type);

                if (set.isEmpty()) {
                    /*
                        no room found. So, remove an order from the overflow Shelf
                     */
                  overflowGroup.removeOneOrderFromShelf();  // TODO: if empty
                    log.debug("Remove an order from overflow shelf when no order with temperature which isn't " + type.toString());
                } else {

                    Optional<FulfilledOrder> findAndRemoveOrderOptional = overflowGroup.findOrderWithTemperatureAndRemoveIt(set);
                    if (findAndRemoveOrderOptional.isEmpty()) {
                        /*
                            such order can't be found
                         */
                        overflowGroup.removeOneOrderFromShelf();  // TODO: if empty
                        log.error("Remove an order from overflow shelf when no an order meets a temperature requirement");
                    } else {
                        FulfilledOrder removedOrder = findAndRemoveOrderOptional.get();
                        switch (removedOrder.getTemp()) {
                            case COLD -> {
                                log.debug("An order," + removedOrder + ", from overflow shelf is added to a COLD shelf");
                                group = shelfMap.get(Type.COLD);
                                group.addOrder(removedOrder);
                            }
                            case FROZEN -> {
                                log.debug("An order," + removedOrder + ", from overflow shelf is added to a FROZEN shelf");
                                group = shelfMap.get(Type.FROZEN);
                                group.addOrder(removedOrder);
                            }
                            case HOT -> {
                                log.debug("An order," + removedOrder + ", from overflow shelf is added to a HOT shelf");
                                group = shelfMap.get(Type.HOT);
                                group.addOrder(removedOrder);
                            }
                        }
                    }
                    overflowGroup.addOrder(newOrder);
                    log.debug("An order, " + newOrder.toString() + ", to an overflow shelf after removing one order");
                }
            } else {
                log.debug("An order, " + newOrder.toString() + ", to an overflow shelf");
            }
        } else {
            log.debug("An order, " + newOrder.toString() + ", to its temperature shelf");
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

    private Set<Temperature> findOrderTemperaturesWithRoomOtherThanTemperature(Type shelfType) {

        Set<Temperature> set = new HashSet<>();
        Set<Type> typeSet = this.shelfMap
                .keySet()
                .stream()
                .filter(t -> !t.equals(Type.OVERFLOW) && !t.equals(shelfType))
                .collect(Collectors.toSet());

        for (Type type : typeSet) {
            ShelfRack shelfRack = shelfMap.get(type);
            if (shelfRack.hasRooms()) {
                switch (type) {
                    case HOT -> set.add(Temperature.HOT);
                    case FROZEN -> set.add(Temperature.FROZEN);
                    case COLD -> set.add(Temperature.COLD);
                }
            }
        }
        return set;
    }

    // -------------------------------------------------- for test only ---------------------------------------

    /**
     * find the number of orders in the shelf rack with the given type
     *
     * @param type
     * @return
     */
    public int getNumberOfOrdersInShelfRack(Type type){

        return this
                .shelfMap
                .get(type)
                .getShelves()
                .stream()
                .map(Shelf::getNumberOfOrders)
                .reduce(0, Integer::sum);
    }

    /**
     * remove an order from a shelf rack with the given type
     *
     * @param type
     * @return
     */
    public Optional<FulfilledOrder> removeOneOrderFromShelfRack(Type type){

        ShelfRack rack = this.shelfMap.get(type);
        return rack.removeOneOrderFromShelf();
    }
}

