# Fulfillment Application Documentation

### Introduction ###

This page is a documentation of a fulfillment system.

### System Design 

#### Domain Objects

* ReceivedOrder - A received order from an external source
* FulfilledOrder - A fulfilled order. Its data is the same as a received order with an additional fulfillment time
* FulfilledOrderSnapShot - A snap shot of the state of a fulfilled order at a given moment: which shelf it is on and 
its expiration value at the moment. 
* Shelf - A business domain object holds a collection of fulfilled orders
* ShelfRack - A business domain object holds a collection of shelves (At this moment, a shelf rack with only one shelf 
has been tested)
* NowOrderEvent - An event object for notifying a new order arrival event
* OrderReadyEvent - An event object for notifying an order is fulfilled event
* AllOrderDataLoadedEvent - An event object for notifying the end of loading order data from a file

#### Service Classes

* LoadDataService - Loading data from a JSon data file to the system
* NewOrderHandler - Handling a new order event, that is to fulfill a new order and place it to a proper shelf based on 
the business requirements
* OrderExpirationInspectionTasks - Inspecting the expiration of orders on shelves and removing any expired orders 
periodically
* OrderReadyHandler - Handling a fulfilled order
* AllOrderDataLoadedHandler - handling the event when all data has been loaded into the system

### Implementation

This fulfillment application is implemented with the latest Spring Boot (2.4.0-M4).

#### Configurable System Parameters

* order.receive.rate_per_second - order receiving rate (per second) (default value 2) 
* shelf.size.regular and shelf.size.overflow - shelf size, regular and overflow (default values 10 and 15 respectively)
* shelf.number - number of shelves in a shelf rack (default value 1)
* order.expiration.check.rate - scheduling rate (default value 5000)
 
All of those configurable system parameters are in the application.yaml file

#### Two Profiles

To be able to test the functionality of this application individually, two profiles are needed. A profile is a named 
logical grouping that may be activated programmatically. One profile, prod, is for production and the other, test, 
is for testing. 

#### Running Time Environment

* Java 15

* Gradle 

#### Run This Application

On command line of the home directory of the source code, run the following command:

> ./gradlew bootRun --args='--spring.profiles.active=prod'

#### Run The Test

Import the source code into an IDE, and run those test classes or test cases individually.





