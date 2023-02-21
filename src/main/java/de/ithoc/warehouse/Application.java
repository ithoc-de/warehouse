package de.ithoc.warehouse;

import de.ithoc.warehouse.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.List;

@SpringBootApplication
@Slf4j
public class Application {

    private final StockRepository stockRepository;
    private final WarehouseRepository warehouseRepository;

    public Application(
            StockRepository stockRepository,
            WarehouseRepository warehouseRepository) {
        this.stockRepository = stockRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
