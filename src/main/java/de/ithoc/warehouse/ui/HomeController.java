package de.ithoc.warehouse.ui;

import de.ithoc.warehouse.domain.UserService;
import de.ithoc.warehouse.domain.model.ClientModel;
import de.ithoc.warehouse.domain.model.ProductModel;
import de.ithoc.warehouse.domain.model.WarehouseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@Slf4j
public class HomeController {

    private final UserService userService;

    public HomeController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping(path = "/home")
    public String warehouse(OAuth2AuthenticationToken authenticationToken, Model model) {

        ClientModel clientModel = userService.client("Madison Gough");
        model.addAttribute("clientName", clientModel.getName());

        List<ProductModel> productModels = clientModel.getProductModels();
        model.addAttribute("productModels", productModels);

        return "home";
    }

}
