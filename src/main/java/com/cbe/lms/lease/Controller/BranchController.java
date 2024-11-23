package com.cbe.lms.lease.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cbe.lms.lease.Model.Branch;
import com.cbe.lms.lease.Service.BranchService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/branch")
public class BranchController {
    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @PostMapping("/addBranch")
    public ResponseEntity<Branch> addtestBranch(@RequestBody Branch branch) throws Exception {
        try {
            Branch newBranch = branchService.addNewBranch(branch);
            return new ResponseEntity<>(newBranch, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getAllBranches")
    public ResponseEntity<Map<String, Object>> getAllBranches(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam int size,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (districtId == null) {
                response = branchService.getAllBranches(page, size, sortBy, sortOrder);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                response = branchService.getBranchsByDistrictId(page, size, districtId, sortBy, sortOrder);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

        } catch (Exception e) {
            // Handle exceptions and return an appropriate response

            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", e.getMessage());
            response.put("path", "/branch/getAllBranches");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // @PutMapping("/updateBranch/{branchId}")
    // public ResponseEntity<Map<String, Object>> updateBranchById(@PathVariable
    // Long branchId,
    // @RequestBody Branch updatedBranch) {
    // try {
    // Branch updated = branchService.updateBranchById(branchId, updatedBranch);
    // Map<String, Object> response = new HashMap<>();
    // response.put("message", "Branch updated successfully");
    // response.put("branch", updated);
    // return ResponseEntity.ok(response);
    // } catch (IllegalArgumentException e) {
    // return ResponseEntity.notFound().build();
    // } catch (Exception e) {
    // // Handle exceptions and return an appropriate response
    // Map<String, Object> errorResponse = new HashMap<>();
    // errorResponse.put("timestamp", LocalDateTime.now());
    // errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    // errorResponse.put("error", "Internal Server Error");
    // errorResponse.put("message", e.getMessage());
    // errorResponse.put("path", "/branch/updateBranch/" + branchId);
    // return
    // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    // }
    // }

    @PutMapping("/updateBranch/{branchId}")
    public ResponseEntity<Branch> updateBranchById(@PathVariable Long branchId,
            @RequestBody Branch updatedBranch) throws Exception {
        updatedBranch.setId(branchId);
        Branch updatedbranchs = branchService.updateBranchById(branchId, updatedBranch);
        if (updatedbranchs != null) {
            return new ResponseEntity<>(updatedbranchs, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // public String putMethodName(@PathVariable String id, @RequestBody String
    // entity) {
    // // TODO: process PUT request

    // return entity;
    // }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBranchById(@PathVariable Long id) throws Exception {
        Map<String, Object> branchData = branchService.getBranchById(id);

        if (branchData != null) {
            return new ResponseEntity<>(branchData, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/deleteBranch/{branchId}")
    public ResponseEntity<String> deleteBranchById(@PathVariable Long branchId) {
        try {
            branchService.deleteBranchById(branchId);
            return ResponseEntity.ok("Branch id " + branchId + " deleted successfully.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Log the exception details for debugging purposes
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getBranchByDistrict/{id}")
    public ResponseEntity<Map<String, Object>> getBranchsByDistrictId(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam int size,
            @PathVariable Long id,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) throws Exception {
        Map<String, Object> branchData = branchService.getBranchsByDistrictId(page, size, id, sortBy, sortOrder);

        if (branchData != null) {
            return new ResponseEntity<>(branchData, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
