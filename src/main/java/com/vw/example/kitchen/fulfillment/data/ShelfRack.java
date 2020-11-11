package com.vw.example.kitchen.fulfillment.data;

import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrder;
import com.vw.example.kitchen.fulfillment.data.domain.FulfilledOrderSnapShot;
import com.vw.example.kitchen.fulfillment.data.domain.Temperature;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class ShelfRack {

    private final Type type;
    private final int numberOfShelves;
    private final List<Shelf> shelves;
    private int idxRunner;

    public ShelfRack(Type type, int numberOfShelves, int shelfSize) {
        assert(numberOfShelves > 0);
        assert(shelfSize > 1);

        this.type = type;
        this.numberOfShelves = numberOfShelves;
        this.shelves = new LinkedList<>();
        for(int i = 0; i< numberOfShelves; i++){
            this.shelves.add(new Shelf(i, shelfSize));
        }
        this.idxRunner = 0;
    }

    /**
     * add an order to this shelf rack. true - success, false - failure
     *
     * @param order
     * @return
     */
    public boolean addOrder(FulfilledOrder order){
        if(numberOfShelves == 1) {
            return shelves.get(0).addOrder(order);
        }

        boolean isAdded = false;
        int counter = 0;
        while(!isAdded && counter < this.numberOfShelves){
            idxRunner = (idxRunner == this.numberOfShelves) ? 0 : idxRunner;
            isAdded = shelves.get(idxRunner++).addOrder(order);
            counter++;
        }
        return isAdded;
    }

    /**
     * find all orders in this shelf rack
     *
     * @return
     */
    public List<FulfilledOrder> getAllFulfilledOrders(){
        return shelves.stream().flatMap(s -> s.getOrders().stream()).collect(Collectors.toList());
    }

    /**
     * find an order with one the given temperatures and remove it
     *
     * @param temperatures
     * @return
     */
    public Optional<FulfilledOrder> findOrderWithTemperatureAndRemoveIt(Set<Temperature> temperatures){
        for(Shelf shelf : shelves){
            Optional<FulfilledOrder> optionalFulfilledOrder = shelf.findOrderWithTemperature(temperatures);
            if(optionalFulfilledOrder.isPresent()){
                if(shelf.removeOrder(optionalFulfilledOrder.get())) {
                    log.debug("Order removed: " + optionalFulfilledOrder.get().toString());
                }else{
                        log.error("Order not removed: " + optionalFulfilledOrder.get().toString());
                }
                return optionalFulfilledOrder;
            }
        }
        return Optional.empty();
    }

    /*


     */

    /**
     * remove expired orders from this shelf rack and return those orders
     *
     * @return
     */
    public Set<FulfilledOrderSnapShot> removeExpiredOrders(){

        Set<FulfilledOrderSnapShot> removedOrders = new HashSet<>();
        for(Shelf shelf : shelves){
            removedOrders.addAll(shelf.removeExpiredOrders(this.type));
        }
        return removedOrders;
    }

    /**
     * find any rooms in this shelf rack
     *
     * @return
     */
    public boolean hasRooms(){
        for(Shelf shelf : shelves){
            if(shelf.hasRoom())
                return true;
        }
        return false;
    }

    /**
     * remove one order from this shelf rack
     *
     * @return
     */
    public Optional<FulfilledOrder> removeOneOrderFromShelf(){

        for(Shelf shelf : shelves){
            if(shelf.getOrders().size() > 0){
                Optional.of(shelf.removeOneOrderFromShelf());
            }
        }
        return Optional.empty();
    }

    /**
     * get a snap shot of orders in this shelf rack
     *
     * @return
     */
    public synchronized Set<FulfilledOrderSnapShot> getSnapShotOfFulfilledOrders(){
        return shelves
                .stream()
                .flatMap( shelf -> shelf.getSnapShotOfFulfilledOrders(type).stream())
                .collect(Collectors.toSet());
    }

    /**
     * remove the given order from this shelf rack
     *
     * @param order
     * @return
     */
    public boolean removeOrder(FulfilledOrder order){

        for(Shelf shelf : shelves){
            if(shelf.removeOrder(order)){
                return true;
            }
        }
        return false;
    }

    /**
     * get the total number of orders in this shelf rack
     *
     * @return
     */
    public int getTotalNumberOfOrders(){

        return shelves.stream().map(shelf -> shelf.getNumberOfOrders()).reduce(0, Integer::sum);
    }
}
