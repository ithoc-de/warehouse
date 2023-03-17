package de.ithoc.warehouse.ui;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
public class AuthenticationController {

    @GetMapping("/")
    public String index() {
        log.debug("/ -> index() -> index");

        return "index";
    }


    @GetMapping("/home")
    public String home() {
        log.debug("/home -> home() -> home");

        return "home";
    }


    @GetMapping("/login")
    public String login() {
        log.debug("/login -> login() -> home");

        return "home";
    }


    @GetMapping(path = "/logout")
    public String logout(Model model) {
        log.debug("/logout -> logout() -> index");

        return "index";
    }



}
