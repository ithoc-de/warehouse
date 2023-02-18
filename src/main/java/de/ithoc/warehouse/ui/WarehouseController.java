package de.ithoc.warehouse.ui;

import de.ithoc.warehouse.domain.Stock;
import de.ithoc.warehouse.domain.Warehouse;
import de.ithoc.warehouse.domain.WarehouseService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
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
    public String warehouse(KeycloakAuthenticationToken authentication, @ModelAttribute Warehouse warehouse) {

        // TODO Implement this controller
        warehouse.setName("My Warehouse");

        return "warehouse";
    }

    @PostMapping(path = "/showStock")
    public String showStock(@RequestParam String stockNumber, @ModelAttribute Stock stock) {

        return "stock";
    }

    @GetMapping(path = "/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        request.logout();
        return "/";
    }

}
