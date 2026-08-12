package com.example.cakeCatalog.controller;

import com.example.cakeCatalog.model.CakeCatalogInfo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.cakeCatalog.service.CakeCatalogService;
import java.math.BigDecimal;
import java.util.List;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/cakecatalog")
public class CakeCatalogController {
    @Autowired
    private CakeCatalogService cakeCatalogService;
    @PostMapping("/addcakeinfo")
    public CakeCatalogInfo addCakeInfo( @Valid @RequestBody CakeCatalogInfo cakeCatalogInfo){
        return cakeCatalogService.addCakeInfo(cakeCatalogInfo);
    }
    @GetMapping("/getallcake")
    public List<CakeCatalogInfo> getAllCakeInfo(){

        return cakeCatalogService.getAllCakeInfo();
    }
    @GetMapping("/getcake/{id}")
    public ResponseEntity<CakeCatalogInfo> getCakeById(@PathVariable int id){
       CakeCatalogInfo cakeInfo=cakeCatalogService.getCakeById(id);
       if(cakeInfo!=null){
          return new ResponseEntity<>(cakeInfo,HttpStatus.OK );
       }
       return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @GetMapping("/filter")
    public List<CakeCatalogInfo> filterCakes(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return cakeCatalogService.filterCakes(name, category, minPrice, maxPrice);
    }

}
