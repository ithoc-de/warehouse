package de.ithoc.warehouse.domain.model;

import lombok.Data;

@Data
public class Stock {

    private String productNumber;
    private String productName;
    private Long quantity;

}
