package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.domain.model.StockModel;
import de.ithoc.warehouse.domain.mapper.StockMapper;
import de.ithoc.warehouse.persistence.entities.Stock;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StockMapperTest {

    @Test
    public void mapFromStockModelToStockEntity() {
        StockModel stock = getStock(71, "Product Name", 1308);

        StockMapper mapper = Mappers.getMapper(StockMapper.class);
        Stock stockEntity = mapper.toStockEntity(stock);

        assertEquals(1308, stockEntity.getQuantity());
    }

    private StockModel getStock(int number, String name, long quantity) {

        StockModel stock = new StockModel();
        stock.setQuantity(quantity);

        return stock;
    }

    private Stock getStockEntity(int number, String name, long quantity) {

        Stock stock = new Stock();
        stock.setQuantity(quantity);

        return stock;
    }

}
