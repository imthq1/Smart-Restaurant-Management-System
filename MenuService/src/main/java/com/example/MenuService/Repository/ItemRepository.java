package com.example.MenuService.Repository;

import com.example.MenuService.Domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    List<Item> findByCategoryId(int categoryId);

    @Query("SELECT i FROM Item i where i.isAvailable is true ")
    List<Item> findByIsAvailableTrue();

    @Query("SELECT i FROM Item i WHERE i.id IN :ids")
    List<Item> findByListIds(@Param("ids") List<Integer> ids);

    @Query("""
    SELECT i FROM Item i
    WHERE (:categoryId IS NULL OR i.category.id = :categoryId)
      AND (:search IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :search, '%')))
      AND (:availableOnly = false OR i.isAvailable = true)
""")
    List<Item> findItems(
            @Param("categoryId") Integer categoryId,
            @Param("search") String search,
            @Param("availableOnly") boolean availableOnly
    );


    List<Item> findByCategoryIdAndIsAvailableTrue(int categoryId);

    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Item> searchByName(@Param("keyword") String keyword);

}