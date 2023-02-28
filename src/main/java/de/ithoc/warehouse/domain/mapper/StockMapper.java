package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.StockModel;
import de.ithoc.warehouse.persistence.entities.Stock;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

@Mapper
public interface StockMapper {

    StockModel toModel(Stock stock);

    @InheritInverseConfiguration
    Stock toEntity(StockModel stockModel);

}
