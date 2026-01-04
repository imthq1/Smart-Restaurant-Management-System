package com.example.MenuService.Repository;

import com.example.MenuService.Domain.Session;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SessionRepository extends CrudRepository<Session, Integer> {
    @Query("""
    select s
    from Session s
    where s.table.id = :tableId
      and s.status = 'ACTIVE'
""")
    Optional<Session> findActiveSessionByTableId(@Param("tableId") int tableId);

}
