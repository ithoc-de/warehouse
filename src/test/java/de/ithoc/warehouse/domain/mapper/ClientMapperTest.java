package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.ClientModel;
import de.ithoc.warehouse.persistence.entities.Client;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ClientMapperTest {

    @Test
    void toModel() {
        Client client = new Client();
        client.setName("Name");

        ClientModel clientModel = Mappers.getMapper(ClientMapper.class).toModel(client);

        assertThat(clientModel.getName()).isEqualTo("Name");
    }

}