package de.ithoc.warehouse.ui;

import de.ithoc.warehouse.domain.synchronization.SyncService;
import de.ithoc.warehouse.external.authprovider.OidcAdminClient;
import de.ithoc.warehouse.external.authprovider.OidcTokenClient;
import de.ithoc.warehouse.external.authprovider.schema.token.Token;
import de.ithoc.warehouse.external.authprovider.schema.users.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class AdminController {

    private final OidcTokenClient oidcTokenClient;
    private final OidcAdminClient oidcAdminClient;
    private final SyncService syncService;

    public AdminController(OidcTokenClient oidcTokenClient, OidcAdminClient oidcAdminClient, SyncService syncService) {
        this.oidcTokenClient = oidcTokenClient;
        this.oidcAdminClient = oidcAdminClient;
        this.syncService = syncService;
    }


    @GetMapping(path = "/userinfo")
    public String warehouse(Model model, Principal principal) {
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) principal;
        OidcUser oidcUser = (OidcUser) authenticationToken.getPrincipal();
        Map<String, Object> claims = oidcUser.getClaims();

        model.addAttribute("name", claims.get("name"));
        model.addAttribute("email", claims.get("email"));

        return "admin/userinfo";
    }

    @GetMapping(path = "/users")
    public String users(Model model) {

        Token token = oidcTokenClient.token();
        // TODO OHO Do token validation here.

        List<User> users = oidcAdminClient.getUsers(token);
        model.addAttribute("users", users);

        return "admin/users";
    }


    @GetMapping(path = "/sync-orders")
    public String syncOrders(Model model) {

        syncService.syncQuantities();
        model.addAttribute("status", "Orders and customer have been synchronized.");

        return "admin/sync-orders";
    }

}
