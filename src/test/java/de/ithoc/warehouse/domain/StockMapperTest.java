package de.ithoc.warehouse.domain;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StockMapperTest {

    @Test
    public void mapFromStockModelToStockEntity() {
        de.ithoc.warehouse.domain.Stock stock = getStock(71, "Product Name", 1308);

        StockMapper mapper = Mappers.getMapper(StockMapper.class);
        de.ithoc.warehouse.persistence.Stock stockEntity = mapper.toStockEntity(stock);

        assertEquals("71", stockEntity.getQuantity());
    }

    private de.ithoc.warehouse.domain.Stock getStock(int number, String name, long quantity) {

        de.ithoc.warehouse.domain.Stock stock = new de.ithoc.warehouse.domain.Stock();
        stock.setQuantity(quantity);

        return stock;
    }

    private de.ithoc.warehouse.persistence.Stock getStockEntity(int number, String name, long quantity) {

        de.ithoc.warehouse.persistence.Stock stock = new de.ithoc.warehouse.persistence.Stock();
        stock.setQuantity(quantity);

        return stock;
    }

}
