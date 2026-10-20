package com.spms.dao;

import com.spms.model.Slot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class SlotDao {

    private final JdbcTemplate jdbc;

    public SlotDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<Slot> MAPPER = (rs, i) -> new Slot(
            rs.getInt("slot_id"),
            rs.getInt("lot_id"),
            rs.getString("slot_number"),
            rs.getString("status"),
            rs.getBoolean("occupied_now"));

    public List<Slot> findByLot(int lotId, LocalDateTime now) {
        Timestamp ts = Timestamp.valueOf(now);
        String sql = "SELECT s.slot_id, s.lot_id, s.slot_number, s.status, " +
                "  EXISTS (SELECT 1 FROM reservations r WHERE r.slot_id = s.slot_id " +
                "    AND r.status = 'CONFIRMED' AND r.start_time <= ? AND r.end_time > ?) AS occupied_now " +
                "FROM slots s WHERE s.lot_id = ? ORDER BY s.slot_number";
        return jdbc.query(sql, MAPPER, ts, ts, lotId);
    }

    public Optional<Slot> findById(int slotId) {
        String sql = "SELECT s.slot_id, s.lot_id, s.slot_number, s.status, FALSE AS occupied_now " +
                "FROM slots s WHERE s.slot_id = ?";
        return jdbc.query(sql, MAPPER, slotId).stream().findFirst();
    }

    /**
     * Locks the slot row until the surrounding transaction ends.
     * Two users booking the same slot at the same moment are processed one after the other,
     * so the overlap check cannot be fooled (NFR-2: prevent duplicate slot allocation).
     * Must be called inside a @Transactional method.
     */
    public Optional<Slot> lockById(int slotId) {
        String sql = "SELECT s.slot_id, s.lot_id, s.slot_number, s.status, FALSE AS occupied_now " +
                "FROM slots s WHERE s.slot_id = ? FOR UPDATE";
        return jdbc.query(sql, MAPPER, slotId).stream().findFirst();
    }
}
