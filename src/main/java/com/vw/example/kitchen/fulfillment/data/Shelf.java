package com.vw.example.kitchen.fulfillment.data;

import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrder;
import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrderSnapShot;
import com.vw.example.kitchen.fulfillment.data.domain.Temperature;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class Shelf {

    private final int id;
    private final int capacity;
    private List<FulfilledOrder> orders;
    /**
     * numberOfOrders has the same value as orders.size(). use it for quick access
     */
    private int numberOfOrders;

    public Shelf(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        this.orders = new CopyOnWriteArrayList<>();
        this.numberOfOrders = 0;
    }

    /**
     *  If the shelf has room, add the order to this shelf and return an empty optional. Otherwise, return it.
     * @param order
     * @return
     */
    public boolean addOrder(FulfilledOrder order){
            if (!hasRoom()) {
                return false;
            }
            boolean result = orders.add(order);
            if(result){
                synchronized (this){
                    numberOfOrders++;
                }
            }
            return result;
    }

    /**
     * check whether there is room for a new order or not. true - yes, false - not
     * @return
     */
    public boolean hasRoom(){
        return this.numberOfOrders < capacity;
    }

    /**
     * based on the type, return all order information on the shelf with the type
     *
     * @param type
     * @return
     */
    public Set<FulfilledOrderSnapShot> removeExpiredOrders(Type type){

        Set<FulfilledOrderSnapShot> removedOrders = new HashSet<>();
        for(FulfilledOrder order : orders){
            float expirationValue = order.calculateExpirationValue(type);
            if(!order.isStillGood(expirationValue)){
                removedOrders.add(new FulfilledOrderSnapShot(type, order, expirationValue));
                boolean result = orders.remove(order);
                if(result){
                    synchronized (this){
                        numberOfOrders--;
                    }
                }
            }
        }
        return removedOrders;
    }

    /**
     * find order with given temperatures
     *
     * @param temperatures
     * @return
     */
    public Optional<FulfilledOrder> findOrderWithTemperature(Set<Temperature> temperatures) {

        for(FulfilledOrder o : orders){
            if(temperatures.contains(o.getTemp())){
               return Optional.of(o);
            }
        }
        return Optional.empty();
    }

    /**
     * remove the given order from the shelf
     *
     * @param fulfilledOrder
     * @return
     */
    public boolean removeOrder(FulfilledOrder fulfilledOrder) {
        boolean result = orders.remove(fulfilledOrder);
        if(result){
            synchronized (this){
                numberOfOrders--;
            }
        }
        return result;
    }

    /**
     * remove one order from the shelf
     *
     * @return
     */
    public FulfilledOrder removeOneOrderFromShelf() {
        FulfilledOrder order = orders.remove(0);
        if(order != null){
            synchronized (this){
                numberOfOrders--;
            }
        }
        return order;
    }

    /**
     * find all order snapshot for a given shelf type
     *
     * @param type
     * @return
     */
    public Set<FulfilledOrderSnapShot> getSnapShotOfFulfilledOrders(Type type){
        return orders
                .stream()
                .map(order -> new FulfilledOrderSnapShot(type, order, order.calculateExpirationValue(type)))
                .collect(Collectors.toSet());
    }
}
