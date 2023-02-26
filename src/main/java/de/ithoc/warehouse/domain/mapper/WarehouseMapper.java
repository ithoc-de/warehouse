package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.WarehouseModel;
import de.ithoc.warehouse.persistence.entities.Warehouse;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = PackageMapper.class)
public interface WarehouseMapper {

    @Mapping(source = "packages", target = "packageModels")
    WarehouseModel toModel(Warehouse warehouse);

    @InheritInverseConfiguration
    Warehouse toEntity(WarehouseModel warehouseModel);

}
