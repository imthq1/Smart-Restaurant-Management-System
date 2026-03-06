package com.example.MenuService.Service;


import com.example.MenuService.Config.SecurityUtil;
import com.example.MenuService.Domain.ReqDTO.TableRequest;
import com.example.MenuService.Domain.ResDTO.SessionResponse;
import com.example.MenuService.Domain.ResDTO.TableResponse;
import com.example.MenuService.Domain.Session;
import com.example.MenuService.Domain.Table;
import com.example.MenuService.Repository.SessionRepository;
import com.example.MenuService.Repository.TableRepository;
import com.example.MenuService.Util.Enum.SessionStatus;
import com.example.MenuService.Util.Enum.StatusTable;
import com.google.zxing.WriterException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final QRCodeService qrCodeService;
    private final SessionRepository sessionRepository;
    private final SecurityUtil securityUtil;
    @Transactional
    public TableResponse createTable(TableRequest request) throws WriterException, IOException {
        Table table = new Table();
        table.setNumberTable(request.getNumberTable());
        table.setCapacity(request.getCapacity());
        table.setStatusTable(StatusTable.AVAILABLE);
        Table savedTable = tableRepository.save(table);

        return mapToResponse(savedTable);
    }
    public void deleteTable(Integer tableId) {
        tableRepository.deleteById(tableId);
    }
    public SessionResponse createOrGetSession(String nameTable) {

        Table table = tableRepository.findByNumberTable(nameTable)
                .orElseThrow(() -> new RuntimeException("TABLE_NOT_FOUND"));

        // Nếu bàn đang có session ACTIVE → dùng lại
        Optional<Session> activeSession =
                sessionRepository.findActiveSessionByTableId(table.getId());

        Session session;
        if (activeSession.isPresent()) {
            session = activeSession.get();
        } else {
            session = new Session();
            session.setSessionToken(UUID.randomUUID().toString());
            session.setTable(table);
            session.setStatus(SessionStatus.ACTIVE);
            sessionRepository.save(session);

            table.setStatusTable(StatusTable.OCCUPIED);
            tableRepository.save(table);
        }

        String accessToken = securityUtil.createTableAccessToken(
                table.getId(), session.getSessionToken());

        String refreshToken = securityUtil.createTableRefreshToken(
                session.getSessionToken());

        return new SessionResponse(accessToken, refreshToken);
    }

    @Transactional
    public TableResponse generateQRCode(int numberTable) throws WriterException, IOException {
        Table table = tableRepository.findById(numberTable)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        String qrCodeImage = qrCodeService.generateTableQRCode(numberTable);
        table.setQrCode(qrCodeImage);

        Table updatedTable = tableRepository.save(table);

        return mapToResponse(updatedTable);
    }

    public TableResponse getTableWithQRImage(int tableId) throws WriterException, IOException {
        Table table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        return mapToResponse(table);
    }

    public List<TableResponse> getAllTablesWithQR() throws WriterException, IOException {
        List<Table> tables = tableRepository.findAll();
        return tables.stream()
                .map(table -> {
                    try {
                        return mapToResponse(table);
                    } catch (WriterException | IOException e) {
                        throw new RuntimeException("Error generating QR code", e);
                    }
                })
                .collect(Collectors.toList());
    }

    private TableResponse mapToResponse(Table table) throws WriterException, IOException {
        // Generate QR code image từ token


        TableResponse response = new TableResponse();
        response.setId(table.getId());
        response.setNumberTable(table.getNumberTable());
        response.setCapacity(table.getCapacity());
        response.setStatus(table.getStatusTable());
        response.setQrCode(table.getQrCode());
        response.setCreatedAt(table.getCreatedAt());

        return response;
    }
    public Optional<Session> getActiveSessionByTable(int tableId) {
        return sessionRepository.findActiveSessionByTable(tableId, SessionStatus.ACTIVE);
    }

}