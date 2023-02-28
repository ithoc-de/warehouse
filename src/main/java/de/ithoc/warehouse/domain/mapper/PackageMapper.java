package de.ithoc.warehouse.domain.mapper;

import de.ithoc.warehouse.domain.model.PackageModel;
import de.ithoc.warehouse.persistence.entities.Package;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = ProductMapper.class)
public interface PackageMapper {

    @Mapping(source = "products", target = "productModels")
    PackageModel toModel(Package aPackage);

    @InheritInverseConfiguration
    Package toEntity(PackageModel packageModel);

}
