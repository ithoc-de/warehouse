package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.persistence.Client;
import de.ithoc.warehouse.persistence.ClientRepository;
import de.ithoc.warehouse.persistence.Warehouse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarehouseService {

    private final ClientRepository clientRepository;
    private final WarehouseMapper warehouseMapper;

    public WarehouseService(ClientRepository clientRepository, WarehouseMapper warehouseMapper) {
        this.clientRepository = clientRepository;
        this.warehouseMapper = warehouseMapper;
    }


    public List<de.ithoc.warehouse.domain.Warehouse> readWarehouses(de.ithoc.warehouse.domain.Client client) {
        Client clientEntity = clientRepository.findByName(client.getName());
        List<Warehouse> warehouseEntities = clientEntity.getWarehouses();

        return warehouseMapper.toWarehouseModels(warehouseEntities);
    }

}
