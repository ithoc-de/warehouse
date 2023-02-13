package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.persistence.Warehouse;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = StockMapper.class,
        collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE
)
public interface WarehouseMapper {

    de.ithoc.warehouse.persistence.Warehouse toWarehouseEntity(de.ithoc.warehouse.domain.Warehouse warehouse);
    de.ithoc.warehouse.domain.Warehouse toWarehouseModel(Warehouse warehouse);

}
