package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.domain.mapper.WarehouseMapper;
import de.ithoc.warehouse.domain.model.WarehouseModel;
import de.ithoc.warehouse.persistence.entities.Package;
import de.ithoc.warehouse.persistence.entities.Warehouse;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WarehouseMapperTest {

    @Test
    public void fromEntityToModel() {
        Warehouse warehouseEntity = new Warehouse();
        warehouseEntity.setName("My Warehouse");
        warehouseEntity.setAPackages(List.of(new Package()));

        WarehouseMapper mapper = Mappers.getMapper(WarehouseMapper.class);
        WarehouseModel warehouse = mapper.toWarehouseModel(warehouseEntity);

        assertEquals("My Warehouse", warehouse.getName());
    }

}
