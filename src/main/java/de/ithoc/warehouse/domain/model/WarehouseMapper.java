package de.ithoc.warehouse.domain.model;

import de.ithoc.warehouse.persistence.Warehouse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = StockMapper.class)
public interface WarehouseMapper {

    de.ithoc.warehouse.persistence.Warehouse toWarehouseEntity(de.ithoc.warehouse.domain.model.Warehouse warehouse);
    de.ithoc.warehouse.domain.model.Warehouse toWarehouseModel(Warehouse warehouse);
    List<de.ithoc.warehouse.domain.model.Warehouse> toWarehouseModels(List<Warehouse> warehouseEntities);

}
