package de.ithoc.warehouse.ui;

import de.ithoc.warehouse.domain.Stock;
import de.ithoc.warehouse.domain.Warehouse;
import de.ithoc.warehouse.domain.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/")
    public String index() {
        return "landing";
    }

    @GetMapping(path = "/warehouse")
    public String warehouse(@ModelAttribute Warehouse warehouse) {

        // TODO Implement this controller
        warehouse.setName("My Warehouse");

        return "warehouse";
    }

    @PostMapping(path = "/stocks")
    public String stocks(@RequestParam String stockNumber, @ModelAttribute Stock stock) {

        return "stock";
    }

}
