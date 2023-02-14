package de.ithoc.warehouse.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Warehouse {

    private String name;
    private List<Stock> stocks = new ArrayList<>();

}
