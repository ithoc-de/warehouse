package de.ithoc.warehouse;

import de.ithoc.warehouse.persistence.*;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
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


    @PostConstruct
    public void init() {

        Stock stock = new Stock(1308L, "Lagerland", 71L);
        stockRepository.save(stock);

        Warehouse warehouse = new Warehouse("Lagerland", List.of(stock));
        warehouseRepository.save(warehouse);
    }

}
