package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.ClientModel;
import de.ithoc.warehouse.persistence.entities.Client;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = { WarehouseMapper.class, ProductMapper.class })
public interface ClientMapper {

    @Mapping(source = "warehouses", target = "warehouseModels")
    @Mapping(source = "products", target = "productModels")
    ClientModel toModel(Client client);

    @InheritInverseConfiguration
    Client toEntity(ClientModel clientModel);

}
