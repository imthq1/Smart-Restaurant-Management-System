package com.example.MenuService.Domain.ResDTO;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TableResponse {
    private int id;
    private String numberTable;
    private int capacity;
    private String status;
    private String qrCode;
    private Instant createdAt;
}