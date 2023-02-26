package de.ithoc.warehouse.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductModel {

    private Long number;
    private String name;
    private String externalId;
    private List<StockModel> stockModels = new ArrayList<>();

}
