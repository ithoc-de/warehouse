package de.ithoc.warehouse.ui;

import de.ithoc.warehouse.external.epages.EpagesClient;
import de.ithoc.warehouse.external.schema.epages.customers.Customers;
import de.ithoc.warehouse.external.schema.epages.customers.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Slf4j
public class ShopController {

    private final EpagesClient epagesClient;

    public ShopController(EpagesClient epagesClient) {
        this.epagesClient = epagesClient;
    }


    @GetMapping(path = "/customers")
    public String getCustomers(Model model) {

        Customers customers = epagesClient.getCustomers();
        List<Item> items = customers.getItems();
        model.addAttribute("items", items);

        return "shop/customers";
    }

}
