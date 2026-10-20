package com.spms.controller;

import com.spms.model.User;
import com.spms.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** MODULE 1: registration, login, logout. */
@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        return "ADMIN".equals(session.getAttribute("role")) ? "redirect:/admin" : "redirect:/search";
    }

    // ---------------- Registration ----------------
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("errors", new HashMap<String, String>());
        model.addAttribute("form", new HashMap<String, String>());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam(name = "name", defaultValue = "") String name,
                           @RequestParam(name = "email", defaultValue = "") String email,
                           @RequestParam(name = "phone", defaultValue = "") String phone,
                           @RequestParam(name = "password", defaultValue = "") String password,
                           @RequestParam(name = "confirmPassword", defaultValue = "") String confirmPassword,
                           Model model) {
        Map<String, String> errors = authService.register(name, email, phone, password, confirmPassword);
        if (errors.isEmpty()) {
            return "redirect:/login?registered";
        }
        // show errors and keep what the user typed (but never send the password back)
        Map<String, String> form = new HashMap<>();
        form.put("name", name);
        form.put("email", email);
        form.put("phone", phone);
        model.addAttribute("errors", errors);
        model.addAttribute("form", form);
        return "register";
    }

    // ---------------- Login / Logout ----------------
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(name = "email", defaultValue = "") String email,
                        @RequestParam(name = "password", defaultValue = "") String password,
                        HttpServletRequest request, Model model) {
        if (email.trim().isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Email and password are required.");
            model.addAttribute("email", email);
            return "login";
        }
        Optional<User> found = authService.login(email, password);
        if (found.isEmpty()) {
            // same message for wrong password and unknown email: do not reveal which one was wrong
            model.addAttribute("error", "Invalid email or password.");
            model.addAttribute("email", email);
            return "login";
        }
        User user = found.get();

        // new session ID on every login (prevents session fixation)
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getName());
        session.setAttribute("role", user.getRole());

        return "ADMIN".equals(user.getRole()) ? "redirect:/admin" : "redirect:/search";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout";
    }
}
