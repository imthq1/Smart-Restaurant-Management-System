package com.example.MenuService.Controller;

import com.example.MenuService.Config.SecurityUtil;
import com.example.MenuService.Domain.ReqDTO.OpenSessionRequest;
import com.example.MenuService.Domain.ReqDTO.RefreshTokenRequest;
import com.example.MenuService.Domain.ResDTO.SessionResponse;
import com.example.MenuService.Domain.Session;
import com.example.MenuService.Domain.Table;
import com.example.MenuService.Repository.SessionRepository;
import com.example.MenuService.Repository.TableRepository;

import com.example.MenuService.Service.TableService;
import com.example.MenuService.Util.Enum.SessionStatus;
import com.example.MenuService.Util.Enum.StatusTable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final TableRepository tableRepository;
    private final SessionRepository sessionRepository;
    private final TableService tableService;
    private final SecurityUtil jwtUtil;

    /**
     * Khách quét QR → mở hoặc lấy session ACTIVE
     */
    @PostMapping("/open")
    public ResponseEntity<SessionResponse> openSession(
            @RequestBody OpenSessionRequest request) {

        Table table = tableRepository.findById(request.getTableCode())
                .orElseThrow(() -> new RuntimeException("TABLE_NOT_FOUND"));

        // tìm session ACTIVE
        Session session = sessionRepository
                .findActiveSessionByTableId(table.getId())
                .orElseGet(() -> {
                    Session s = new Session();
                    s.setSessionToken(UUID.randomUUID().toString());
                    s.setTable(table);
                    s.setStatus(SessionStatus.ACTIVE);
                    sessionRepository.save(s);

                    table.setStatusTable(StatusTable.OCCUPIED);
                    tableRepository.save(table);
                    return s;
                });

        String accessToken = jwtUtil.createTableAccessToken(
                table.getId(), session.getSessionToken());

        String refreshToken = jwtUtil.createTableRefreshToken(
                session.getSessionToken());

        return ResponseEntity.ok(
                new SessionResponse(accessToken, refreshToken)
        );
    }


//    /**
//     * Token hết hạn → refresh để order tiếp
//     */
//    @PostMapping("/refresh-token")
//    public ResponseEntity<SessionResponse> refreshToken(
//            @RequestHeader("X-Session-Id") String sessionId) {
//
//        Session session = sessionRepository.findById(sessionId)
//                .orElseThrow(() -> new RuntimeException("SESSION_NOT_FOUND"));
//
//        if (!"ACTIVE".equals(session.getStatus())) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
//        }
//
//        Table table = session.getTable();
//
//        String newAccessToken = jwtUtil.createTableAccessToken(
//                table.getId(), session.getId());
//
//        return ResponseEntity.ok(
//                new SessionResponse(newAccessToken, null)
//        );
//    }
//

    /**
     * Thanh toán → đóng session
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeSession(@PathVariable int id) {

        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SESSION_NOT_FOUND"));

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(Instant.now());
        sessionRepository.save(session);

        Table table = session.getTable();
        table.setStatusTable(StatusTable.AVAILABLE);
        tableRepository.save(table);

        return ResponseEntity.ok().build();
    }

    /**
     * (Optional) Lấy session ACTIVE theo table
     */
    @GetMapping("/active/{tableId}")
    public ResponseEntity<Session> getActiveSession(@PathVariable int tableId) {
        return sessionRepository
                .findActiveSessionByTableId(tableId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
