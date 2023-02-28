package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.PackageModel;
import de.ithoc.warehouse.domain.model.ProductModel;
import de.ithoc.warehouse.domain.model.WarehouseModel;
import de.ithoc.warehouse.persistence.entities.Package;
import de.ithoc.warehouse.persistence.entities.Product;
import de.ithoc.warehouse.persistence.entities.Warehouse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WarehouseMapperTest {

    @Mock
    private PackageMapper packageMapper;

    @InjectMocks
    private final WarehouseMapper warehouseMapper = Mappers.getMapper(WarehouseMapper.class);


    @Test
    public void toModel() {
        when(packageMapper.toModel(any(Package.class))).thenReturn(new PackageModel());

        Warehouse warehouse = new Warehouse();
        warehouse.setId(7L);
        warehouse.setName("Name");
        warehouse.setPackages(List.of(new Package(), new Package()));

        WarehouseModel warehouseModel = warehouseMapper.toModel(warehouse);

        assertThat(warehouseModel.getName()).isEqualTo("Name");
        assertThat(warehouseModel.getPackageModels().size()).isEqualTo(2);
    }

}
