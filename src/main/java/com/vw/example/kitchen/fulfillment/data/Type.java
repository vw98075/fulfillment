package com.vw.example.kitchen.fulfillment.data;


public enum Type {

    FROZEN("frozen"),
    COLD("cold"),
    HOT("hot"),
    OVERFLOW("overflow");

    public final String label;

    Type(String label) {
        this.label = label;
    }

}
