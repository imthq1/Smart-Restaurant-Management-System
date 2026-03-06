package com.example.MenuService.Domain;

import com.example.MenuService.Util.Enum.StatusTable;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@jakarta.persistence.Table(name = "tables")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Table {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "number_table", nullable = false)
    private String numberTable;

    @Column(
            name = "qr_code",
            unique = true,
            length = 2048
    )
    private String qrCode;

    @Column(nullable = false)
    private int capacity;



    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Enumerated(EnumType.STRING)
    private StatusTable statusTable;
    // Relationship: One Table has Many Sessions
    // cascade = CascadeType.ALL: Khi xóa Table thì xóa tất cả Sessions liên quan
    // orphanRemoval = true: Khi remove session khỏi list thì tự động xóa trong DB
    // JsonManagedReference: Tránh vòng lặp vô hạn khi serialize JSON (phía cha)
    @OneToMany(mappedBy = "table", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Session> sessions = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    public void addSession(Session session) {
        sessions.add(session);
        session.setTable(this);
    }

    public void removeSession(Session session) {
        sessions.remove(session);
        session.setTable(null);
    }
}
