package com.vw.example.kitchen.fulfillment;

import com.vw.example.kitchen.fulfillment.data.domain.ShelfRack;
import com.vw.example.kitchen.fulfillment.data.Type;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.EnumMap;
import java.util.Map;


@SpringBootApplication
@EnableScheduling
@EnableAsync
@Slf4j
public class FulfillmentApplication {

	@Value("${shelf.size.regular:10}")
	private int regularShelfSize;

	@Value("${shelf.size.overflow:15}")
	private int overflowShelfSize;

	@Value("${shelf.number:1}")
	private int numberOfShelves;

	public static void main(String[] args) {

		SpringApplication.run(FulfillmentApplication.class, args);
	}

	@Bean
	public Map<Type, ShelfRack> shelfMap(){

		Map<Type, ShelfRack> shelfMap = new EnumMap<>(Type.class);
		shelfMap.put(Type.COLD, new ShelfRack(Type.COLD, numberOfShelves, regularShelfSize));
		shelfMap.put(Type.FROZEN, new ShelfRack(Type.FROZEN, numberOfShelves, regularShelfSize));
		shelfMap.put(Type.HOT, new ShelfRack(Type.HOT, numberOfShelves, regularShelfSize));
		shelfMap.put(Type.OVERFLOW, new ShelfRack(Type.OVERFLOW, numberOfShelves, overflowShelfSize));
		return shelfMap;
	}
}
