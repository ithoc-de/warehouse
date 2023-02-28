package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.ClientModel;
import de.ithoc.warehouse.domain.model.PackageModel;
import de.ithoc.warehouse.domain.model.WarehouseModel;
import de.ithoc.warehouse.persistence.entities.Client;
import de.ithoc.warehouse.persistence.entities.Package;
import de.ithoc.warehouse.persistence.entities.Warehouse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClientMapperTest {

    @Mock
    private WarehouseMapper warehouseMapper;

    @InjectMocks
    private final ClientMapper clientMapper = Mappers.getMapper(ClientMapper.class);


    @Test
    public void toModel() {
        when(warehouseMapper.toModel(any(Warehouse.class))).thenReturn(new WarehouseModel());

        Client client = new Client();
        client.setId(7L);
        client.setName("Name");
        client.setWarehouses(List.of(new Warehouse(), new Warehouse()));

        ClientModel clientModel = clientMapper.toModel(client);

        assertThat(clientModel.getName()).isEqualTo("Name");
        assertThat(clientModel.getWarehouseModels().size()).isEqualTo(2);
    }

}
