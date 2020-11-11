package com.vw.example.kitchen.fulfillment.data.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Temperature {

    FROZEN("frozen"),
    COLD("cold"),
    HOT("hot");

    @JsonValue
    public final String label;

    Temperature(String label) {
        this.label = label;
    }
}
