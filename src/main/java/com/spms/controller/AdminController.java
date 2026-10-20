package com.spms.controller;

import com.spms.dao.ParkingLotDao;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

/** Simple admin landing page. Access is limited to the ADMIN role by LoginInterceptor. */
@Controller
public class AdminController {

    private final ParkingLotDao lotDao;

    public AdminController(ParkingLotDao lotDao) {
        this.lotDao = lotDao;
    }

    @GetMapping("/admin")
    public String dashboard(Model model) {
        model.addAttribute("lots", lotDao.findAll(LocalDateTime.now()));
        return "admin";
    }
}
