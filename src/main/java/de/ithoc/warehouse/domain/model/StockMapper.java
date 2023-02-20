package de.ithoc.warehouse.domain.model;

import de.ithoc.warehouse.persistence.Stock;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    de.ithoc.warehouse.persistence.Stock toStockEntity(de.ithoc.warehouse.domain.model.Stock stock);
    de.ithoc.warehouse.domain.model.Stock toStockModel(Stock stock);

    List<de.ithoc.warehouse.domain.model.Stock> toStocks(List<de.ithoc.warehouse.persistence.Stock> stockEntities);

}
