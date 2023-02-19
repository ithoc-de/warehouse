package de.ithoc.warehouse.ui;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Map;

@Controller
@Slf4j
public class AdminController {

    @GetMapping(path = "/userinfo")
    public String warehouse(Model model, Principal principal) {
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) principal;
        OidcUser oidcUser = (OidcUser) authenticationToken.getPrincipal();
        Map<String, Object> claims = oidcUser.getClaims();

        model.addAttribute("name", claims.get("name"));
        model.addAttribute("email", claims.get("email"));

        return "userinfo";
    }

}
