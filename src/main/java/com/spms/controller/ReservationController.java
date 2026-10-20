package com.spms.controller;

import com.spms.dao.ParkingLotDao;
import com.spms.dao.ReservationDao;
import com.spms.dao.SlotDao;
import com.spms.model.ParkingLot;
import com.spms.model.Reservation;
import com.spms.model.Slot;
import com.spms.service.BookingException;
import com.spms.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

/** MODULE 2 (part 2): reserve, view bookings, cancel (FR-4, Use Case 2 and 5). */
@Controller
public class ReservationController {

    private final SlotDao slotDao;
    private final ParkingLotDao lotDao;
    private final ReservationDao reservationDao;
    private final ReservationService reservationService;

    public ReservationController(SlotDao slotDao, ParkingLotDao lotDao,
                                 ReservationDao reservationDao, ReservationService reservationService) {
        this.slotDao = slotDao;
        this.lotDao = lotDao;
        this.reservationDao = reservationDao;
        this.reservationService = reservationService;
    }

    // ---------------- Reserve ----------------
    @GetMapping("/reserve/{slotId}")
    public String reserveForm(@PathVariable("slotId") int slotId, Model model) {
        loadSlotInfo(slotId, model);
        model.addAttribute("vehicleNo", "");
        model.addAttribute("start", LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0).toString());
        model.addAttribute("hours", "1");
        return "reserve";
    }

    @PostMapping("/reserve/{slotId}")
    public String reserve(@PathVariable("slotId") int slotId,
                          @RequestParam(name = "vehicleNo", defaultValue = "") String vehicleNo,
                          @RequestParam(name = "start", defaultValue = "") String start,
                          @RequestParam(name = "hours", defaultValue = "") String hours,
                          HttpSession session, Model model) {
        int userId = (Integer) session.getAttribute("userId");
        try {
            int reservationId = reservationService.reserve(userId, slotId, vehicleNo, start, hours);
            return "redirect:/bookings/" + reservationId + "/confirmation";
        } catch (BookingException e) {
            loadSlotInfo(slotId, model);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("vehicleNo", vehicleNo);
            model.addAttribute("start", start);
            model.addAttribute("hours", hours);
            return "reserve";
        }
    }

    // ---------------- Bookings ----------------
    @GetMapping("/bookings")
    public String bookings(HttpSession session, Model model) {
        int userId = (Integer) session.getAttribute("userId");
        model.addAttribute("bookings", reservationDao.findByUser(userId));
        return "bookings";
    }

    @GetMapping("/bookings/{id}/confirmation")
    public String confirmation(@PathVariable("id") int id, HttpSession session, Model model) {
        int userId = (Integer) session.getAttribute("userId");
        Reservation r = reservationDao.findByIdForUser(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        model.addAttribute("r", r);
        return "confirmation";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable("id") int id, HttpSession session, RedirectAttributes redirect) {
        int userId = (Integer) session.getAttribute("userId");
        try {
            reservationService.cancel(userId, id);
            redirect.addFlashAttribute("success", "Booking #" + id + " cancelled. The slot is free again.");
        } catch (BookingException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/bookings";
    }

    // ---------------- helper ----------------
    private void loadSlotInfo(int slotId, Model model) {
        Slot slot = slotDao.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking slot not found"));
        ParkingLot lot = lotDao.findById(slot.getLotId(), LocalDateTime.now())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parking lot not found"));
        model.addAttribute("slot", slot);
        model.addAttribute("lot", lot);
    }
}
