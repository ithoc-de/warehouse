package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.persistence.Warehouse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = StockMapper.class)
public interface WarehouseMapper {

    de.ithoc.warehouse.persistence.Warehouse toWarehouseEntity(de.ithoc.warehouse.domain.Warehouse warehouse);
    de.ithoc.warehouse.domain.Warehouse toWarehouseModel(Warehouse warehouse);
    List<de.ithoc.warehouse.domain.Warehouse> toWarehouseModels(List<Warehouse> warehouseEntities);

}
