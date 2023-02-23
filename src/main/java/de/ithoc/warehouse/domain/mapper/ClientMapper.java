package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.ClientModel;
import de.ithoc.warehouse.persistence.entities.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    ClientModel toModel(Client client);

}
