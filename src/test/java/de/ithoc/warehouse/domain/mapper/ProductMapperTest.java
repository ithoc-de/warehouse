package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.ProductModel;
import de.ithoc.warehouse.domain.model.StockModel;
import de.ithoc.warehouse.persistence.entities.Product;
import de.ithoc.warehouse.persistence.entities.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductMapperTest {

    @Mock
    private StockMapper stockMapper;

    @InjectMocks
    private final ProductMapper productMapper = Mappers.getMapper(ProductMapper.class);

    public ProductMapperTest() {
    }

    @Test
    public void toModel() {
        int stockCount = 3;
        List<Stock> stocks = new ArrayList<>(stockCount);
        for (int i = 0; i < stockCount; i++) {
            Stock stock = new Stock();
            stock.setId((long) (i + 1));
            stock.setQuantity((long) ((i + 1) * 10));
            stock.setValidFrom(
                    LocalDateTime.of(2022, Month.JANUARY, 1, 1, 1, 1, 1));
            stock.setUpdatedBy("" + i);
            stocks.add(stock);
        }
        StockModel stockModel = new StockModel();
        stockModel.setQuantity(11L);
        when(stockMapper.toModel(any(Stock.class))).thenReturn(stockModel);

        String externalId = UUID.randomUUID().toString();
        Product product = new Product();
        product.setId(7L);
        product.setNumber("11");
        product.setName("Produktname");
        product.setExternalId(externalId);
        product.setStocks(stocks);

        ProductModel productModel = productMapper.toModel(product);

        assertThat(productModel.getNumber()).isEqualTo("11");
        assertThat(productModel.getName()).isEqualTo("Produktname");
        assertThat(productModel.getExternalId()).isEqualTo(externalId);
        assertThat(productModel.getStockModels().size()).isEqualTo(stockCount);
    }

}
