package com.vw.example.kitchen.fulfillment.data.domain;

import com.vw.example.kitchen.fulfillment.data.Temperature;
import lombok.*;

@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ReceivedOrder {

    private String id;
    private String name;
    private Temperature temp;
    protected int shelfLife;
    protected float decayRate;

    public ReceivedOrder(String id, String name, Temperature temp, int shelfLife, float decayRate) {
        this.id = id;
        this.name = name;
        this.temp = temp;
        this.shelfLife = shelfLife;
        this.decayRate = decayRate;
    }
}