package de.ithoc.warehouse.ui;

import de.ithoc.warehouse.persistence.Stock;
import de.ithoc.warehouse.persistence.StockRepository;
import de.ithoc.warehouse.persistence.Warehouse;
import de.ithoc.warehouse.persistence.WarehouseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;
    private final StockRepository stockRepository;

    public WarehouseController(WarehouseRepository warehouseRepository, StockRepository stockRepository) {
        this.warehouseRepository = warehouseRepository;
        this.stockRepository = stockRepository;
    }

    @GetMapping({"/", "/warehouse"})
    public String warehouse(@ModelAttribute Warehouse warehouse) {

        Warehouse w = warehouseRepository.findAll().get(0);
        warehouse.setName(w.getName());
        warehouse.setStocks(w.getStocks());

        return "warehouse";
    }

    @PostMapping("/showStock")
    public String showStock(@RequestParam String stockNumber, @ModelAttribute StockModel stockModel) {

        Stock s = stockRepository.findByProductNumber(stockNumber).orElseThrow();

        stockModel.setQuantity(s.getQuantity());
        stockModel.setProductNumber(s.getProductNumber() + "");
        stockModel.setProductName(s.getProductName());

        return "stock";
    }

}
