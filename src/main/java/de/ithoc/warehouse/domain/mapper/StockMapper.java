package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.StockModel;
import de.ithoc.warehouse.persistence.entities.Stock;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMapper {

    Stock toStockEntity(StockModel stockModel);
    StockModel toStockModel(Stock stock);

    List<StockModel> toStocks(List<Stock> stocks);

}
