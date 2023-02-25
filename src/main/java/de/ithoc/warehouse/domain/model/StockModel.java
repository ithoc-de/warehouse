package de.ithoc.warehouse.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StockModel {

    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    private Long quantity;

}
