package de.ithoc.warehouse.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductModel {

    private Long lineNo;
    private String number;
    private String image;
    private String name;
    private String externalId;
    private Long quantity;

    private List<StockModel> stockModels = new ArrayList<>();

}
