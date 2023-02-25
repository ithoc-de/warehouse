package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.domain.mapper.WarehouseMapper;
import de.ithoc.warehouse.persistence.repositories.ClientRepository;
import org.springframework.stereotype.Service;

@Service
public class WarehouseService {

    private final ClientRepository clientRepository;
    private final WarehouseMapper warehouseMapper;

    public WarehouseService(ClientRepository clientRepository, WarehouseMapper warehouseMapper) {
        this.clientRepository = clientRepository;
        this.warehouseMapper = warehouseMapper;
    }

}
