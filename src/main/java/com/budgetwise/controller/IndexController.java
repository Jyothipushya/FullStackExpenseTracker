/*package com.budgetwise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")  // ← ADD THIS LINE
    public String root() {
        return "home";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}*/

package com.budgetwise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")  // Root URL shows home.html
    public String root() {
        return "index";
    }

    @GetMapping("/index")
    public String home() {
        return "index";
    }
    // ADD THESE MAPPINGS:
   /* @GetMapping("/login")
    public String login() {
        return "login";  // Returns templates/login.html
    }

    @GetMapping("/register")
    public String register() {
        return "register";  // Returns templates/register.html
    }*/
}
