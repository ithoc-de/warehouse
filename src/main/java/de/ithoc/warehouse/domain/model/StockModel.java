package de.ithoc.warehouse.domain.model;

import lombok.Data;

@Data
public class StockModel {

    private String productId;
    private String productName;
    private String productImage;

    private Long quantity;
    private String unit;

}
