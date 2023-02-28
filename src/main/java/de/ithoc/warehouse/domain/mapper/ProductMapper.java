package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.ProductModel;
import de.ithoc.warehouse.domain.model.StockModel;
import de.ithoc.warehouse.persistence.entities.Product;
import de.ithoc.warehouse.persistence.entities.Stock;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = StockMapper.class)
public interface ProductMapper {

    @Mapping(source = "stocks", target = "stockModels")
    ProductModel toModel(Product product);

    @InheritInverseConfiguration
    Product toEntity(ProductModel productModel);

}
