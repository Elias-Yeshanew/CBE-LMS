package com.example.demo.lease.Controller;

import com.example.demo.lease.Model.District;
import com.example.demo.lease.Service.DistrictService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/districts")
public class DistrictController {

    private final DistrictService districtService;

    public DistrictController(DistrictService districtService) {
        this.districtService = districtService;
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getDistrictCount() {
        long count = districtService.getDistrictCount();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @PostMapping("/addDistrict")
    public ResponseEntity<District> addDistrict(@RequestBody District district) throws Exception {
        try {
            District newDistrict = districtService.addNewDistrict(district);
            return new ResponseEntity<>(newDistrict, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/getAllDistricts")
    public ResponseEntity<Map<String, Object>> getAllDistricts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam int size) {
        try {
            Map<String, Object> response = districtService.getAllDistricts(page, size);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("path", "/district/getAllDistricts");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateDistrict/{districtId}")
    public ResponseEntity<Map<String, Object>> updateDistrictById(@PathVariable Long districtId,
            @RequestBody District updatedDistrict) {
        try {
            District updated = districtService.updateDistrictById(districtId, updatedDistrict);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "District updated successfully");
            response.put("district", updated);
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
            errorResponse.put("path", "/district/updateDistrict/" + districtId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDistrictById(@PathVariable Long id) throws Exception {
        Map<String, Object> districtData = districtService.getDistrictById(id);

        if (districtData != null) {
            return new ResponseEntity<>(districtData, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/deleteDistrict/{districtId}")
    public ResponseEntity<String> deleteDistrictById(@PathVariable Long districtId) {
        try {
            districtService.deleteDistrictById(districtId);
            return new ResponseEntity<>("District id " + districtId + " deleted successfully", HttpStatus.OK);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
