package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.mapper.StockMapper;
import de.ithoc.warehouse.domain.model.WarehouseModel;
import de.ithoc.warehouse.persistence.entities.Warehouse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = StockMapper.class)
public interface WarehouseMapper {

    Warehouse toWarehouseEntity(WarehouseModel warehouse);
    WarehouseModel toWarehouseModel(Warehouse warehouse);
    List<WarehouseModel> toWarehouseModels(List<Warehouse> warehouses);

}
