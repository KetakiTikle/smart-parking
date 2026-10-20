# Smart Parking Management System (PBL project)

Java 17 + Spring Boot 3 + Thymeleaf + MySQL 8.

* **Module 1 - User Registration and Login** (FR-1, FR-2, NFR-3)
* **Module 2 - Parking Search and Slot Reservation** (FR-3, FR-4, NFR-2, NFR-5, Use Case 5)

## 1. Prerequisites
| Tool | Version | Check with |
|---|---|---|
| JDK | 17 or newer | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| MySQL Server | 8.0.16+ | MySQL Workbench |

## 2. Set up the database
1. Open MySQL Workbench and connect to your local server.
2. Open `database/schema.sql` and run all of it (lightning icon, or Ctrl+Shift+Enter).
3. The last query should list 4 parking lots with their slot counts (12, 15, 10, 20 slots).

## 3. Configure
Open `src/main/resources/application.properties` and change `spring.datasource.password` to your MySQL root password.

## 4. Run
```
mvn spring-boot:run
```
Open http://localhost:8080. The demo accounts are created automatically on first start:

| Role | Email | Password |
|---|---|---|
| Admin | admin@parking.com | Admin@123 |
| Driver | driver@test.com | Driver@123 |

## 5. Run unit tests
```
mvn test
```
Runs 68 unit tests (validation rules, overlap rule, cost, cancellation rule). No database is needed for these.

## 6. Project structure
```
database/schema.sql                      database + seed data
src/main/java/com/spms/
  SmartParkingApplication.java           entry point
  config/   WebConfig, LoginInterceptor, DataInitializer
  controller/  AuthController, ParkingController, ReservationController, AdminController
  service/     AuthService, ReservationService, BookingException
  dao/         UserDao, ParkingLotDao, SlotDao, ReservationDao   (JdbcTemplate, prepared statements)
  model/       User, ParkingLot, Slot, Reservation
  util/        ValidationUtil, ReservationRules                  (pure logic, unit tested)
src/main/resources/templates/            Thymeleaf pages
src/test/java/com/spms/                  JUnit 5 tests
```
Flow of a request: Browser -> Controller -> Service -> DAO -> MySQL.

## 7. Troubleshooting
| Problem | Fix |
|---|---|
| `Access denied for user 'root'` | Wrong password in `application.properties` |
| `Unknown database 'smart_parking'` | Run `database/schema.sql` first |
| `Port 8080 already in use` | Change `server.port` in `application.properties` |
| `release version 17 not supported` | Install JDK 17 or newer and make sure `JAVA_HOME` points to it |
| `Schema.sql` fails on the `WITH RECURSIVE` line | MySQL is older than 8.0; upgrade MySQL |
| To reset all data | Run `database/schema.sql` again and restart the app |

## 8. Known limitations (mention as future scope in the report)
Payment, QR tickets, admin reports/slot management, CSRF protection, and account lockout are not implemented.
