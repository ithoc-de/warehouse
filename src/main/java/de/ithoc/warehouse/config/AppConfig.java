package de.ithoc.warehouse.config;

import de.ithoc.warehouse.domain.WarehouseMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public WarehouseMapper warehouseMapper() {

        return Mappers.getMapper(WarehouseMapper.class);
    }

}
