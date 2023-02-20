package de.ithoc.warehouse.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Warehouse {

    private String username;

    private String name;
    private List<Stock> stocks = new ArrayList<>();

}
