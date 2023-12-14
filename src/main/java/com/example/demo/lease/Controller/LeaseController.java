package com.example.demo.lease.Controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.demo.lease.GenerateReportsRequestBody;
import com.example.demo.lease.Model.Lease;
import com.example.demo.lease.Repository.LeaseRepository;
import com.example.demo.lease.Service.BranchService;
import com.example.demo.lease.Service.DistrictService;
import com.example.demo.lease.Service.LeaseService;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;

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

    @Value("${file.upload-dir}")
    private String uploadDir;

    public LeaseController(LeaseService leaseService, BranchService branchService, DistrictService districtService) {
        this.leaseService = leaseService;
        this.branchService = branchService;
        this.districtService = districtService;
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
            } else if (startYear != null && endYear == null) {
                // Filter with only start date
                response = leaseService.getLeasesByContractYear(startYear, DEFAULT_END_YEAR, page, size);
            } else if (startYear == null && endYear != null) {
                // Filter with only end date
                response = leaseService.getLeasesByContractYear(DEFAULT_START_YEAR, endYear, page, size);
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

    @GetMapping("/expiredLeases")
    public Map<String, Object> getExpiredLeases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam int size) {
        return leaseService.getAllExpiredLeases(page, size);
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<Lease> updateLease(
            @PathVariable Long id,
            @RequestBody Lease lease) {
        lease.setId(id);
        Lease updatedLease = leaseService.updateLease(id, lease);
        if (updatedLease != null) {
            return new ResponseEntity<>(updatedLease, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadImage(
            @RequestPart("file") MultipartFile imageFile) {

        // Assuming you have a storage folder path defined in your properties
        String storageFolderPath = uploadDir;

        try {
            // Get the original filename
            String fileNameVariable = imageFile.getOriginalFilename();

            // Move the file to the storage folder
            Path destinationPath = Paths.get(storageFolderPath, fileNameVariable);
            Files.copy(imageFile.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            // Prepare the response
            Map<String, String> response = new HashMap<>();
            response.put("message", "File uploaded");
            response.put("fileName", fileNameVariable);
            response.put("storageFolderPath", storageFolderPath);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "File upload failed"));
        }
    }

    private List<Map<String, Object>> mapLeasesToResponse(List<Lease> leases) {
        List<Map<String, Object>> leasesWithBranchName = new ArrayList<>();
        for (Lease lease : leases) {
            Map<String, Object> leaseData = new HashMap<>();
            leaseData.put("districtName", lease.getBranch().getDistrict().getDistrictName());
            leaseData.put("branchName", lease.getBranch().getBranchName());
            leaseData.put("id", lease.getId());
            leaseData.put("discountRate", lease.getDiscountRate());
            leaseData.put("branchName", lease.getBranch().getBranchName());
            leaseData.put("contractStartDate", lease.getContractStartDate());
            leaseData.put("contractEndDate", lease.getContractEndDate());
            leaseData.put("totalPayment", lease.getTotalPayment());
            leaseData.put("advancePayment", lease.getAdvancePayment());
            leaseData.put("initialDirectCost", lease.getInitialDirectCost());
            leaseData.put("leaseIncentive", lease.getLeaseIncentive());
            leaseData.put("authorization", lease.isAuthorization());
            leaseData.put("installmentDetails", lease.getInstallmentDetails());
            leaseData.put("contractRegisteredDate", lease.getContractRegisteredDate());
            leaseData.put("contractType", lease.getContractType());
            leaseData.put("fileName", lease.getFileName());
            leaseData.put("contractReason", lease.getContractReason());
            leasesWithBranchName.add(leaseData);
        }
        return leasesWithBranchName;
    }

    @GetMapping("/byBranchId/{branchId}")
    public ResponseEntity<Object> getLeasesByBranchId(@PathVariable Long branchId,
            @RequestParam(required = false, defaultValue = "0") int startYear) {
        List<Lease> leases;
        if (startYear != 0) {
            // Filter leases based on the provided contract registration year
            leases = leaseRepository.findByBranchIdAndContractRegisteredDateContaining(branchId,
                    startYear);
        } else {
            leases = leaseRepository.findByBranchId(branchId);
        }
    if (leases.isEmpty()) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "No leases found for branch ID: " + branchId);
        return ResponseEntity.notFound().build();
    } else {
        Map<String, Object> response = new HashMap<>();
    response.put("leases", mapLeasesToResponse(leases));
    return ResponseEntity.ok(response);
}
}

@GetMapping("/by-branch/{branchId}")
public ResponseEntity<List<Lease>> getLeasesByBranchId(
        @PathVariable Long branchId,
        @RequestParam(required = false) Integer startDate) {

    List<Lease> leases;

    if (startDate != null) {
        // Use the converted value in your service method
        leases = leaseService.getLeasesByBranchIdAndContractRegisteredYear(branchId, startDate);
    } else {
        leases = leaseService.getLeasesByBranchId(branchId);
    }

    return new ResponseEntity<>(leases, HttpStatus.OK);
    }

    // @GetMapping("/byBranchId/{branchId}")
    // public ResponseEntity<Object> getLeasesByBranchId(@PathVariable Long
    // branchId,
    // @RequestParam(required = false, defaultValue = "0") int startYear) {
    // List<Lease> leases;

    // if (startYear != 0) {
    // // Filter leases based on the provided contract registration year
    // leases =
    // leaseRepository.findByBranchIdAndContractRegisteredDateYear(branchId,
    // startYear);
    // } else {
    // leases = leaseRepository.findByBranchId(branchId);
    // }

    // if (leases.isEmpty()) {
    // Map<String, String> response = new HashMap<>();
    // response.put("message", "No leases found for branch ID: " + branchId);
    // return ResponseEntity.notFound().build();
    // } else {
    // Map<String, Object> response = new HashMap<>();
    // response.put("leases", mapLeasesToResponse(leases));
    // return ResponseEntity.ok(response);
    // }
    // }

    @GetMapping("/byDistrictId/{districtId}")
    public ResponseEntity<Object> getLeasesByDistrictId(@PathVariable Long districtId) {
        List<Lease> leases = leaseRepository.findByDistrictIdQuery(districtId);

        if (leases.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No leases found for district ID: " + districtId);
            return ResponseEntity.notFound().build();
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("leases", mapLeasesToResponse(leases));
            return ResponseEntity.ok(response);
        }
    }

}
