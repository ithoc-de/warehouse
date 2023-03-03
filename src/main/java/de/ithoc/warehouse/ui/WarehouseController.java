package de.ithoc.warehouse.ui;

import de.ithoc.warehouse.domain.ProductService;
import de.ithoc.warehouse.domain.UserService;
import de.ithoc.warehouse.domain.model.ClientModel;
import de.ithoc.warehouse.domain.model.ProductModel;
import de.ithoc.warehouse.domain.model.StockModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@Slf4j
public class WarehouseController {

    private final UserService userService;
    private final ProductService productService;

    public WarehouseController(UserService userService, ProductService productService) {
        this.userService = userService;
        this.productService = productService;
    }


    @GetMapping(path = "/warehouse")
    public String warehouse(OAuth2AuthenticationToken authenticationToken, Model model) {

        ClientModel clientModel = userService.client("Madison Gough");
        model.addAttribute("clientName", clientModel.getName());

        List<ProductModel> productModels = clientModel.getProductModels();
        model.addAttribute("productModels", productModels);

        return "warehouse/warehouse";
    }


    @GetMapping(path = "/stock")
    public String stock(Model model, @RequestParam String externalId) {
        log.debug("externalId: {}", externalId);

        StockModel stockModel = productService.readProductStock(externalId);
        model.addAttribute("stockModel", stockModel);

        return "warehouse/stock";
    }


    @PostMapping(path = "/modify")
    public String minus(Model model, @ModelAttribute StockModel stockModel, @RequestParam String arithmetic) {
        Long quantity = stockModel.getQuantity();
        switch (arithmetic) {
            case "minus":
                quantity -= 1;
                break;
            case "plus":
                quantity += 1;
                break;
            default:
                break;
        }
        productService.writeQuantity(quantity, stockModel.getProductId());
        stockModel.setQuantity(quantity);

        return "warehouse/stock";
    }


}
