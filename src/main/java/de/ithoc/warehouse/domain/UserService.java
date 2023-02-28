package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.domain.mapper.ClientMapper;
import de.ithoc.warehouse.domain.model.ClientModel;
import de.ithoc.warehouse.domain.model.WarehouseModel;
import de.ithoc.warehouse.persistence.entities.Client;
import de.ithoc.warehouse.persistence.repositories.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public UserService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }


    public ClientModel client(String companyName) {

        Optional<Client> clientOptional = clientRepository.findByName(companyName);
        if(clientOptional.isEmpty()) {
            return null;
        }

        Client client = clientOptional.get();
        log.debug("client: {}", client);

        ClientModel clientModel = clientMapper.toModel(client);
        log.debug("clientModel: {}", clientModel);

        return clientModel;
    }

}
