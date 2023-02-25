package de.ithoc.warehouse.domain.model;

import java.util.ArrayList;
import java.util.List;

public class ProductModel {

    private Long number;
    private String name;
    private String externalId;
    private List<StockModel> stocks = new ArrayList<>();

}
