package com.spms.dao;

import com.spms.model.ParkingLot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ParkingLotDao {

    private final JdbcTemplate jdbc;

    public ParkingLotDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** available_now = active slots with no confirmed booking covering the given moment. */
    private static final String BASE_SQL =
            "SELECT l.lot_id, l.name, l.location, l.address, l.total_slots, l.price_per_hour, " +
            "  (SELECT COUNT(*) FROM slots s WHERE s.lot_id = l.lot_id AND s.status = 'ACTIVE' " +
            "     AND NOT EXISTS (SELECT 1 FROM reservations r WHERE r.slot_id = s.slot_id " +
            "        AND r.status = 'CONFIRMED' AND r.start_time <= ? AND r.end_time > ?)) AS available_now " +
            "FROM parking_lots l ";

    private static final RowMapper<ParkingLot> MAPPER = (rs, i) -> new ParkingLot(
            rs.getInt("lot_id"),
            rs.getString("name"),
            rs.getString("location"),
            rs.getString("address"),
            rs.getInt("total_slots"),
            rs.getBigDecimal("price_per_hour"),
            rs.getInt("available_now"));

    /** Partial, case-insensitive match on location or lot name. */
    public List<ParkingLot> search(String term, LocalDateTime now) {
        String like = "%" + escapeLike(term.trim()) + "%";
        Timestamp ts = Timestamp.valueOf(now);
        return jdbc.query(BASE_SQL + "WHERE l.location LIKE ? OR l.name LIKE ? ORDER BY l.location, l.name",
                MAPPER, ts, ts, like, like);
    }

    public List<ParkingLot> findAll(LocalDateTime now) {
        Timestamp ts = Timestamp.valueOf(now);
        return jdbc.query(BASE_SQL + "ORDER BY l.location, l.name", MAPPER, ts, ts);
    }

    public Optional<ParkingLot> findById(int lotId, LocalDateTime now) {
        Timestamp ts = Timestamp.valueOf(now);
        return jdbc.query(BASE_SQL + "WHERE l.lot_id = ?", MAPPER, ts, ts, lotId).stream().findFirst();
    }

    /** Stops a user typing % or _ from acting as a wildcard. */
    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
