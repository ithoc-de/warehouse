package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.domain.model.WarehouseMapper;
import de.ithoc.warehouse.persistence.Stock;
import de.ithoc.warehouse.persistence.Warehouse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WarehouseMapperTest {

    @Test
    public void fromEntityToModel() {
        List<Stock> stockEntities = new ArrayList<>();
        for(int i = 0; i < 2; i++) {
            stockEntities.add(getStockEntity((i + 1), UUID.randomUUID().toString(), (i * 2)));
        }
        Warehouse warehouseEntity = new Warehouse();
        warehouseEntity.setName("My Warehouse");
        warehouseEntity.setStocks(stockEntities);

        WarehouseMapper mapper = Mappers.getMapper(WarehouseMapper.class);
        de.ithoc.warehouse.domain.model.Warehouse warehouse = mapper.toWarehouseModel(warehouseEntity);

        assertEquals("My Warehouse", warehouse.getName());
        assertEquals(2, warehouse.getStocks().size());
    }


    private Stock getStockEntity(int number, String name, long quantity) {

        Stock stock = new Stock();
        stock.setQuantity(quantity);

        return stock;
    }

}
