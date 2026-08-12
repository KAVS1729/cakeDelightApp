package com.example.cakeCatalog.service;

import com.example.cakeCatalog.model.CakeCatalogInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.cakeCatalog.repository.CakeCatalog;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CakeCatalogService {
    @Autowired
    private CakeCatalog cakeCatalog;

    public CakeCatalogInfo addCakeInfo(CakeCatalogInfo cakeCatalogInfo){

        return cakeCatalog.save(cakeCatalogInfo);
    }
    public List<CakeCatalogInfo> getAllCakeInfo(){

        return cakeCatalog.findAll();
    }

    public CakeCatalogInfo getCakeById(int id) {
        return cakeCatalog.findById(id).orElse(null);
    }
    public List<CakeCatalogInfo> filterCakes(String name, String category, BigDecimal minPrice, BigDecimal maxPrice) {
        List<CakeCatalogInfo> allCakes = cakeCatalog.findAll();

        return allCakes.stream()
                .filter(cake -> name == null || name.isBlank()
                        || cake.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(cake -> category == null || category.isBlank()
                        || cake.getCategory().equalsIgnoreCase(category))
                .filter(cake -> minPrice == null
                        || cake.getPrice().compareTo(minPrice) >= 0)
                .filter(cake -> maxPrice == null
                        || cake.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
    }
}

