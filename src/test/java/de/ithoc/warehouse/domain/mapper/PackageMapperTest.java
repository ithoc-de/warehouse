package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.PackageModel;
import de.ithoc.warehouse.domain.model.ProductModel;
import de.ithoc.warehouse.domain.model.StockModel;
import de.ithoc.warehouse.persistence.entities.Package;
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
public class PackageMapperTest {

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private final PackageMapper packageMapper = Mappers.getMapper(PackageMapper.class);


    @Test
    public void toModel() {
        when(productMapper.toModel(any(Product.class))).thenReturn(new ProductModel());

        String externalId = UUID.randomUUID().toString();
        Package aPackage = new Package();
        aPackage.setProducts(List.of(new Product(), new Product()));
        aPackage.setName("Name");
        aPackage.setId(7L);
        aPackage.setExternalId(externalId);

        PackageModel packageModel = packageMapper.toModel(aPackage);

        assertThat(packageModel.getExternalId()).isEqualTo(externalId);
        assertThat(packageModel.getName()).isEqualTo("Name");
        assertThat(packageModel.getProductModels().size()).isEqualTo(2);
    }

}
