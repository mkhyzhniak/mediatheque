package com.mediatheque.mediatheque.controller;

import com.mediatheque.mediatheque.dto.UserDTO;
import com.mediatheque.mediatheque.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new UserDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute UserDTO user, Model model) {
        if (user.getUsername().isEmpty() || user.getPassword().isEmpty()) {
            model.addAttribute("error", "Username and password are required");
            model.addAttribute("user", user);
            return "register";
        }

        boolean success = userService.registerUser(user);
        if (!success) {
            model.addAttribute("error", "Username already taken");
            model.addAttribute("user", user);
            return "register";
        }

        return "redirect:/login?registered";
    }
}
