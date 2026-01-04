package com.example.MenuService.Service;


import com.example.MenuService.Config.SecurityUtil;
import com.example.MenuService.Domain.ReqDTO.TableRequest;
import com.example.MenuService.Domain.ResDTO.TableResponse;
import com.example.MenuService.Domain.Table;
import com.example.MenuService.Repository.TableRepository;
import com.google.zxing.WriterException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final QRCodeService qrCodeService;
    private final SecurityUtil securityUtil;
    @Transactional
    public TableResponse createTable(TableRequest request) throws WriterException, IOException {
        Table table = new Table();
        table.setNumberTable(request.getNumberTable());
        table.setCapacity(request.getCapacity());
        table.setStatus("AVAILABLE");
        String qrCodeImage = qrCodeService.generateTableQRCode(request.getNumberTable());
        table.setQrCode(qrCodeImage);
        Table savedTable = tableRepository.save(table);

        return mapToResponse(savedTable);
    }
    public void deleteTable(Integer tableId) {
        tableRepository.deleteById(tableId);
    }
    @Transactional
    public TableResponse generateQRCode(String numberTable) throws WriterException, IOException {
        Table table = tableRepository.findByNumberTable(numberTable)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        String qrCodeImage = qrCodeService.generateTableQRCode(table.getNumberTable());
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
        response.setStatus(table.getStatus());
        response.setQrCode(table.getQrCode());
        response.setCreatedAt(table.getCreatedAt());

        return response;
    }
}