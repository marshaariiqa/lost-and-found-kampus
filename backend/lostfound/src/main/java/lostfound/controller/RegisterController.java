package lostfound.controller;

import lostfound.model.Pengguna;
import lostfound.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class RegisterController {

    @Autowired
    private AuthService authService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("pengguna", new Pengguna());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute("pengguna") Pengguna pengguna,
            Model model) {

        try {
            authService.register(pengguna);
            return "redirect:/login?registered";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
