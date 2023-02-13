package de.ithoc.warehouse.domain;

import lombok.Data;

@Data
public class Stock {

    private String productNumber;
    private String productName;
    private Long quantity;

}
