package de.ithoc.warehouse.ui;

import lombok.Data;

@Data
public class StockModel {

    private String productNumber;
    private String productName;
    private Long quantity;

}
