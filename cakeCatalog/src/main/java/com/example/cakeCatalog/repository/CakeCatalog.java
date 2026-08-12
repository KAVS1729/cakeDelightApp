package com.example.cakeCatalog.repository;

import com.example.cakeCatalog.model.CakeCatalogInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CakeCatalog extends JpaRepository<CakeCatalogInfo,Integer> {
   /* List<CakeCatalogInfo> findByCategory(String category);

    List<CakeCatalogInfo> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<CakeCatalogInfo> findByNameContainingIgnoreCase(String name);*/
}
