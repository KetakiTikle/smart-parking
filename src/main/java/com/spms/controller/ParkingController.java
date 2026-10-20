package com.spms.controller;

import com.spms.dao.ParkingLotDao;
import com.spms.dao.SlotDao;
import com.spms.model.ParkingLot;
import com.spms.util.ValidationUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

/** MODULE 2 (part 1): search parking lots and view slot availability (FR-3). */
@Controller
public class ParkingController {

    private final ParkingLotDao lotDao;
    private final SlotDao slotDao;

    public ParkingController(ParkingLotDao lotDao, SlotDao slotDao) {
        this.lotDao = lotDao;
        this.slotDao = slotDao;
    }

    @GetMapping("/search")
    public String search(@RequestParam(name = "q", required = false) String q, Model model) {
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("searched", false);
        if (q == null) {
            return "search";                       // first visit: just show the form
        }
        String error = ValidationUtil.validateLocation(q);
        if (error != null) {
            model.addAttribute("error", error);    // empty or invalid characters: no query is run
            return "search";
        }
        model.addAttribute("lots", lotDao.search(q, LocalDateTime.now()));
        model.addAttribute("searched", true);
        return "search";
    }

    @GetMapping("/lots/{lotId}/slots")
    public String slots(@PathVariable("lotId") int lotId, Model model) {
        LocalDateTime now = LocalDateTime.now();
        ParkingLot lot = lotDao.findById(lotId, now)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking lot not found"));
        model.addAttribute("lot", lot);
        model.addAttribute("slots", slotDao.findByLot(lotId, now));
        return "slots";
    }
}
