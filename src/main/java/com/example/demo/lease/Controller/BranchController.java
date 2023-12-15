package com.example.demo.lease.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody; // Add this import

import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Service.BranchService;

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
            @RequestParam int size) {
        try {
            Map<String, Object> response = branchService.getAllBranches(page, size);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Handle exceptions and return an appropriate response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("path", "/branch/getAllBranches");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/updateBranch/{branchId}")
    public ResponseEntity<Map<String, Object>> updateBranchById(@PathVariable Long branchId,
            @RequestBody Branch updatedBranch) {
        try {
            Branch updated = branchService.updateBranchById(branchId, updatedBranch);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Branch updated successfully");
            response.put("branch", updated);
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
            errorResponse.put("path", "/branch/updateBranch/" + branchId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBranchById(@PathVariable Long id) throws Exception {
        Map<String, Object> branchData = branchService.getBranchById(id);

        if (branchData != null) {
            return new ResponseEntity<>(branchData, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
