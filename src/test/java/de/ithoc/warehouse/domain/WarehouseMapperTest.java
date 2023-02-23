package de.ithoc.warehouse.domain;

import de.ithoc.warehouse.domain.model.WarehouseMapper;
import de.ithoc.warehouse.persistence.entities.ProductPackage;
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
        warehouseEntity.setProductPackages(List.of(new ProductPackage()));

        WarehouseMapper mapper = Mappers.getMapper(WarehouseMapper.class);
        de.ithoc.warehouse.domain.model.Warehouse warehouse = mapper.toWarehouseModel(warehouseEntity);

        assertEquals("My Warehouse", warehouse.getName());
    }

}
