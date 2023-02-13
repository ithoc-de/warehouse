package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.persistence.Stock;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    de.ithoc.warehouse.persistence.Stock toStockEntity(de.ithoc.warehouse.domain.Stock stock);
    de.ithoc.warehouse.domain.Stock toStockModel(Stock stock);

    List<de.ithoc.warehouse.domain.Stock> toStocks(List<de.ithoc.warehouse.persistence.Stock> stockEntities);

}
