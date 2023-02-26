package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.domain.mapper.ClientMapper;
import de.ithoc.warehouse.domain.model.ClientModel;
import de.ithoc.warehouse.domain.model.WarehouseModel;
import de.ithoc.warehouse.persistence.entities.Client;
import de.ithoc.warehouse.persistence.entities.Warehouse;
import de.ithoc.warehouse.persistence.repositories.ClientRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private UserService userService;


    @Test
    public void client() {
        Client client = new Client();
        client.setId(7L);
        client.setName("Name");
        client.setWarehouses(List.of(new Warehouse(), new Warehouse()));
        when(clientRepository.findByName(eq("Name"))).thenReturn(Optional.of(client));

        ClientModel clientModel = new ClientModel();
        clientModel.setName("Name");
        clientModel.setWarehouseModels(List.of(new WarehouseModel(), new WarehouseModel()));
        when(clientMapper.toModel(any(Client.class))).thenReturn(clientModel);

        ClientModel actualClientModel = userService.client("Name");

        assertThat(actualClientModel.getName()).isEqualTo("Name");
        assertThat(actualClientModel.getWarehouseModels().size()).isEqualTo(2);

        verify(clientRepository, times(1)).findByName("Name");
        verify(clientMapper, times(1)).toModel(any(Client.class));
    }


    @Test
    public void clientNull() {
        when(clientRepository.findByName(eq("Name"))).thenReturn(Optional.empty());

        ClientModel actualClientModel = userService.client("Name");

        assertThat(actualClientModel).isNull();

        verify(clientRepository, times(1)).findByName("Name");
        verify(clientMapper, times(0)).toModel(any(Client.class));
    }

}
