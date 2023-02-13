package de.ithoc.warehouse.ui;

import de.ithoc.warehouse.domain.Stock;
import de.ithoc.warehouse.domain.Warehouse;
import de.ithoc.warehouse.domain.WarehouseService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping({"/", "/warehouse"})
    public String warehouse(@ModelAttribute Warehouse warehouse) {


        return "warehouse";
    }

    @PostMapping("/showStock")
    public String showStock(@RequestParam String stockNumber, @ModelAttribute Stock stock) {

        return "stock";
    }

}
