package com.example.demo.lease.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.lease.Model.Region;
import com.example.demo.lease.Service.RegionService;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.AllArgsConstructor;

@CrossOrigin
@AllArgsConstructor
@RestController
@RequestMapping("/region")
public class RegionController {

    private final RegionService regionService;

    @PostMapping
    public ResponseEntity<Region> addNewRegion(@RequestBody Region region) throws Exception {
        try {
            Region newRegion = regionService.addNewRegion(region);
            return new ResponseEntity<>(newRegion, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/updateRegion/{regionId}")
    public ResponseEntity<Map<String, Object>> updateRegionById(@PathVariable Long regionId,
            @RequestBody Region updatedRegion) {
        try {
            Region updated = regionService.updateRegionById(regionId, updatedRegion);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Region updated successfully");
            response.put("region", updated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Handle exceptions and return an appropriate response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("path", "/region/updateRegion/" + regionId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/getAllRegions")
    public ResponseEntity<Map<String, Object>> getAllRegions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam int size) {
        try {
            Map<String, Object> response = regionService.getAllRegions(page, size);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Handle exceptions and return an appropriate response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("path", "/region/getAllRegions");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
