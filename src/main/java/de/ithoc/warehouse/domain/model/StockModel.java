package de.ithoc.warehouse.domain.model;

import lombok.Data;

@Data
public class StockModel {

    private long lineNo;
    private Long quantity;
    private String unit;

}
