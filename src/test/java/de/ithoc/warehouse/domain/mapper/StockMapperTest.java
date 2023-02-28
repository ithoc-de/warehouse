package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.StockModel;
import de.ithoc.warehouse.persistence.entities.Stock;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class StockMapperTest {

    private final StockMapper mapper = Mappers.getMapper(StockMapper.class);

    @Test
    public void toEntity() {
        StockModel stockModel = createModel(1308);

        Stock stockEntity = mapper.toEntity(stockModel);

        assertEquals(1308, stockEntity.getQuantity());
    }

    @Test
    public void toModel() {
        Stock stock = new Stock();
        stock.setQuantity(12L);
        stock.setValidFrom(LocalDateTime.now());
        stock.setUpdatedBy("Me");

        StockModel stockModel = mapper.toModel(stock);

        assertThat(stockModel.getQuantity()).isEqualTo(12);
    }

    private StockModel createModel(long quantity) {

        StockModel stock = new StockModel();
        stock.setQuantity(quantity);

        return stock;
    }

    private Stock createEntity(int number, String name, long quantity) {

        Stock stock = new Stock();
        stock.setQuantity(quantity);

        return stock;
    }

}
