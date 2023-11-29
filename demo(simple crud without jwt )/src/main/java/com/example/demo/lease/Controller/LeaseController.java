package com.example.demo.lease.Controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.lease.GenerateReportsRequestBody;
import com.example.demo.lease.Model.Lease;
import com.example.demo.lease.Repository.LeaseRepository;
import com.example.demo.lease.Service.BranchService;
import com.example.demo.lease.Service.DistrictService;
import com.example.demo.lease.Service.LeaseService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;

@CrossOrigin
@RestController
@RequestMapping("/leases")
public class LeaseController {

    private static final int DEFAULT_START_YEAR = 0;
    private static final int DEFAULT_END_YEAR = 0;
    private final LeaseService leaseService;
    private final BranchService branchService;
    private final DistrictService districtService;

    @Autowired
    private LeaseRepository leaseRepository;

    public LeaseController(LeaseService leaseService, BranchService branchService, DistrictService districtService) {
        this.leaseService = leaseService;
        this.branchService = branchService;
        this.districtService = districtService;
    }

    @GetMapping("/byBranchId/{branchId}")
    public List<Lease> getLeasesByBranchId(@PathVariable Long branchId) {
        List<Lease> leases = leaseService.getLeasesByBranchId(branchId);

        for (Lease lease : leases) {
            String installmentDetails = lease.getInstallmentDetails(); // Retrieve the JSON string from the entity
            if (installmentDetails != null) {

                lease.setInstallmentDetails(installmentDetails);
            }
        }

        return leases;
    }

    @GetMapping("/byBranchId")
    public List<Lease> getLeasesByBranchIds(@RequestBody List<Long> branchIds) {
        List<Lease> allLeases = new ArrayList<>();

        for (Long branchId : branchIds) {
            List<Lease> leasesForBranch = leaseRepository.findByBranchId(branchId);
            allLeases.addAll(leasesForBranch);
        }

        return allLeases;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllLeases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam int size,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear) {
        try {
            Map<String, Object> response;

            if (startYear != null && endYear != null) {
                // Filter between two dates
                response = leaseService.getLeasesByContractYearRange(startYear, endYear, page, size);
            } else if (startYear != null) {
                // Filter with only start date
                response = leaseService.getLeasesByContractYearRange(startYear, DEFAULT_END_YEAR, page, size);
            } else if (endYear != null) {
                // Filter with only end date
                response = leaseService.getLeasesByContractYearRange(DEFAULT_START_YEAR, endYear, page, size);
            } else {
                // No date filter, get all leases
                response = leaseService.getAllLeases(page, size);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Handle exceptions and return an appropriate response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("path", "/leases");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getLeaseWithBranchById(@PathVariable Long id) {
        Map<String, Object> leaseData = leaseService.getLeaseById(id);

        if (leaseData != null) {
            return new ResponseEntity<>(leaseData, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Lease> addNewLease(@RequestBody Lease lease) throws Exception {
        try {
            Lease createdLease = leaseService.addNewLease(lease);
            return new ResponseEntity<>(createdLease, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/unauthorized")
    public List<Lease> getUnauthorizedLeases() {
        return leaseService.getUnauthorizedLeases();
    }

    // @GetMapping("/expiredLeases")
    // public ResponseEntity<Map<String, Object>>
    // getExpiredLeasesWithAdditionalFilter(
    // @RequestParam(defaultValue = "1") int page,
    // @RequestParam int size,
    // @RequestParam(required = false) Integer startYear,
    // @RequestParam(required = false) Integer endYear) {
    // try {
    // Map<String, Object> response;

    // if (startYear != null && endYear != null) {
    // // Filter expired leases with additional filter
    // response = leaseService.getAllExpiredLeasesWithAdditionalFilter(page, size,
    // startYear, endYear);
    // } else if (startYear != null) {
    // // Filter with only start date
    // response = leaseService.getLeasesByContractYearRange(startYear,
    // DEFAULT_END_YEAR, page, size);
    // } else if (endYear != null) {
    // // Filter with only end date
    // response = leaseService.getLeasesByContractYearRange(DEFAULT_START_YEAR,
    // endYear, page, size);
    // } else {
    // // If no additional filter, return all expired leases
    // response = leaseService.getAllExpiredLeases(page, size);
    // }

    // return new ResponseEntity<>(response, HttpStatus.OK);
    // } catch (Exception e) {
    // // Handle exceptions and return an appropriate response
    // Map<String, Object> errorResponse = new HashMap<>();
    // errorResponse.put("timestamp", LocalDateTime.now());
    // errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    // errorResponse.put("error", "Internal Server Error");
    // errorResponse.put("message", e.getMessage());
    // errorResponse.put("path", "/api/leases/expiredLeasesWithAdditionalFilter");
    // return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }
    @GetMapping("/expiredLeases")
    public Map<String, Object> getExpiredLeases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam int size) {
        return leaseService.getAllExpiredLeases(page, size);
    }
    // public ResponseEntity<Map<String, Object>>
    // getActiveLeasesWithAdditionalFilter(
    // @RequestParam(defaultValue = "1") int page,
    // @RequestParam int size,
    // @RequestParam(required = false) Integer registeredYear,
    // @RequestParam(required = false) Integer endYear) {
    // try {
    // Map<String, Object> response;

    // if (registeredYear != null && endYear != null) {
    // // Filter active leases with additional filter
    // response = leaseService.getAllActiveLeasesWithAdditionalFilter(page, size,
    // registeredYear, endYear);
    // } else {
    // // If no additional filter, return all active leases
    // response = leaseService.getAllActiveLeases(page, size);
    // }

    // return new ResponseEntity<>(response, HttpStatus.OK);
    // } catch (Exception e) {
    // // Handle exceptions and return an appropriate response
    // Map<String, Object> errorResponse = new HashMap<>();
    // errorResponse.put("timestamp", LocalDateTime.now());
    // errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    // errorResponse.put("error", "Internal Server Error");
    // errorResponse.put("message", e.getMessage());
    // errorResponse.put("path", "/api/leases/activeLeasesWithAdditionalFilter");
    // return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }

    @GetMapping("/activeContracts")
    public Map<String, Object> getAllActiveLeases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam int size) {
        return leaseService.getAllActiveLeases(page, size);
    }

    @PutMapping("/{id}/authorize")
    public void authorizeLease(@PathVariable("id") Long leaseId) throws NotFoundException {
        leaseService.authorizeLeaseById(leaseId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lease> updateLease(@PathVariable Long id, @RequestBody Lease lease) {
        lease.setId(id);
        Lease updatedLease = leaseService.updateLease(lease);
        if (updatedLease != null) {
            return new ResponseEntity<>(updatedLease, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<JSONObject> deleteLease(@PathVariable Long id) {
        try {
            JSONObject response = leaseService.deleteLease(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            JSONObject errorResponse = new JSONObject();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/report")
    public String generateReports(@RequestBody GenerateReportsRequestBody requestBody,
            @RequestParam(required = false, defaultValue = "-1") int selectedYear,
            @RequestParam(required = false, defaultValue = "-1") int selectedMonth) {
        return leaseService.generateReports(requestBody.getType(), requestBody.getTerm(), selectedYear, selectedMonth,
                requestBody.getIds()).toString();
    }

    @GetMapping("/static")
    Map<String, Long> getAllContractsInfo() {
        long totalContracts = leaseService.getTotalContracts();
        long totalUnexpiredContracts = leaseService.getTotalUnexpiredContracts();
        long totalExpiredContracts = leaseService.getTotalExpiredContracts();
        long totalNumberOfBranchs = branchService.getBranchCount();
        long totalNUmberOfDistricts = districtService.getDistrictCount();

        Map<String, Long> contractInfo = new HashMap<>();
        contractInfo.put("totalContracts", totalContracts);
        contractInfo.put("activeContracts", totalUnexpiredContracts);
        contractInfo.put("expiredContracts", totalExpiredContracts);
        contractInfo.put("totalNumberOfBranchs", totalNumberOfBranchs);
        contractInfo.put("totalNUmberOfDistricts", totalNUmberOfDistricts);
        return contractInfo;
    }

    @PostMapping("/reports/all")
    public String generateReportsForAllLeases(
            @RequestBody GenerateReportsRequestBody requestBody,
            @RequestParam(required = false, defaultValue = "-1") int selectedYear,
            @RequestParam(required = false, defaultValue = "-1") int selectedMonth) {
        return leaseService
                .generateReportsForAll(requestBody.getType(), requestBody.getTerm(), selectedYear, selectedMonth)
                .toString();
    }

}
