package com.spms.dao;

import com.spms.model.Reservation;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationDao {

    private final JdbcTemplate jdbc;

    public ReservationDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String SELECT_WITH_NAMES =
            "SELECT r.reservation_id, r.user_id, r.slot_id, r.vehicle_no, r.start_time, r.end_time, " +
            "       r.total_amount, r.status, l.name AS lot_name, s.slot_number " +
            "FROM reservations r " +
            "JOIN slots s ON r.slot_id = s.slot_id " +
            "JOIN parking_lots l ON s.lot_id = l.lot_id ";

    private static final RowMapper<Reservation> MAPPER = (rs, i) -> {
        Reservation r = new Reservation(
                rs.getInt("reservation_id"),
                rs.getInt("user_id"),
                rs.getInt("slot_id"),
                rs.getString("vehicle_no"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime(),
                rs.getBigDecimal("total_amount"),
                rs.getString("status"));
        r.setLotName(rs.getString("lot_name"));
        r.setSlotNumber(rs.getString("slot_number"));
        return r;
    };

    /** Counts confirmed bookings on this slot that overlap [start, end). */
    public int countOverlapping(int slotId, LocalDateTime start, LocalDateTime end) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM reservations " +
                "WHERE slot_id = ? AND status = 'CONFIRMED' AND start_time < ? AND end_time > ?",
                Integer.class, slotId, Timestamp.valueOf(end), Timestamp.valueOf(start));
        return count == null ? 0 : count;
    }

    /** Inserts a CONFIRMED reservation and returns its generated id. */
    public int insert(int userId, int slotId, String vehicleNo,
                      LocalDateTime start, LocalDateTime end, BigDecimal amount) {
        String sql = "INSERT INTO reservations (user_id, slot_id, vehicle_no, start_time, end_time, total_amount, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED')";
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setInt(2, slotId);
            ps.setString(3, vehicleNo);
            ps.setTimestamp(4, Timestamp.valueOf(start));
            ps.setTimestamp(5, Timestamp.valueOf(end));
            ps.setBigDecimal(6, amount);
            return ps;
        }, keys);
        return keys.getKey().intValue();
    }

    public List<Reservation> findByUser(int userId) {
        return jdbc.query(SELECT_WITH_NAMES + "WHERE r.user_id = ? ORDER BY r.start_time DESC", MAPPER, userId);
    }

    /** Returns the booking only if it belongs to this user. */
    public Optional<Reservation> findByIdForUser(int reservationId, int userId) {
        return jdbc.query(SELECT_WITH_NAMES + "WHERE r.reservation_id = ? AND r.user_id = ?",
                MAPPER, reservationId, userId).stream().findFirst();
    }

    /**
     * Soft cancel in ONE atomic statement: only your own, CONFIRMED, not-yet-started booking changes.
     * Returns 1 if cancelled, 0 if any condition failed. The row is never deleted.
     */
    public int cancel(int reservationId, int userId, LocalDateTime now) {
        return jdbc.update(
                "UPDATE reservations SET status = 'CANCELLED', cancelled_at = ? " +
                "WHERE reservation_id = ? AND user_id = ? AND status = 'CONFIRMED' AND start_time > ?",
                Timestamp.valueOf(now), reservationId, userId, Timestamp.valueOf(now));
    }
}
