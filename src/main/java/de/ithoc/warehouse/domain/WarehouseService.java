package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.persistence.Client;
import de.ithoc.warehouse.persistence.ClientRepository;
import de.ithoc.warehouse.persistence.Warehouse;
import org.springframework.stereotype.Service;

@Service
public class WarehouseService {

    private final ClientRepository clientRepository;
    private final WarehouseMapper warehouseMapper;

    public WarehouseService(ClientRepository clientRepository, WarehouseMapper warehouseMapper) {
        this.clientRepository = clientRepository;
        this.warehouseMapper = warehouseMapper;
    }


    public de.ithoc.warehouse.domain.Warehouse readWarehouse(String clientId) {

        Client client = clientRepository.findById(clientId).orElseThrow();
        Warehouse warehouse = client.getWarehouse();

        return warehouseMapper.toWarehouseModel(warehouse);
    }

}
