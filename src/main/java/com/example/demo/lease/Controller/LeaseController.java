package com.example.demo.lease.Controller;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.lease.GenerateReportsRequestBody;
import com.example.demo.lease.Model.Lease;
import com.example.demo.lease.Service.BranchService;
import com.example.demo.lease.Service.DistrictService;
import com.example.demo.lease.Service.LeaseService;

import jakarta.annotation.PostConstruct;

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
    // private LeaseRepository leaseRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private String storageFolderPath;

    @PostConstruct
    private void init() {
        storageFolderPath = uploadDir;
    }

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
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String sortByBranch) {
        try {
            Map<String, Object> response = new HashMap<>();

            if (startYear != null && endYear != null) {
                // Filter between two dates
                response = leaseService.getLeasesByContractYearRange(startYear, endYear,
                        page, size, sortBy, sortOrder);
            } else if (startYear != null && endYear == null) {
                // Filter with only start date
                response = leaseService.getLeasesByContractYear(startYear, DEFAULT_END_YEAR,
                        page, size, sortBy, sortOrder);
            } else if (startYear == null && endYear != null) {
                // Filter with only end date
                response = leaseService.getLeasesByContractYear(DEFAULT_START_YEAR, endYear,
                        page, size, sortBy, sortOrder);
            } else if (sortBy != null && sortOrder != null) {
                if (sortBy.equals("branchName")) {
                    response = leaseService.getAllLeasesSortedByBranchName(page, size, sortOrder);

                } else {
                    response = leaseService.getAllLeasesSorted(page, size, sortBy, sortOrder);

                }
                // filter with other lease properties like contract start date or contract
                // registeration date etc
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

    // @GetMapping
    // public ResponseEntity<Map<String, Object>> getAllLeasess(
    // @RequestParam(defaultValue = "1") int page,
    // @RequestParam int size,
    // @RequestParam(required = false) String sortBy,
    // @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
    // try {
    // Page<Lease> leases = leaseService.getAllLeasesSorted(page, size, sortBy,
    // sortOrder);
    // Map<String, Object> response = new HashMap<>();
    // response.put("leases", leases.getContent());
    // response.put("currentPage", leases.getNumber() + 1);
    // response.put("totalItems", leases.getTotalElements());
    // response.put("totalPages", leases.getTotalPages());
    // return new ResponseEntity<>(response, HttpStatus.OK);
    // } catch (Exception e) {
    // Map<String, Object> errorResponse = new HashMap<>();
    // errorResponse.put("timestamp", LocalDateTime.now());
    // errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    // errorResponse.put("error", "Internal Server Error");
    // errorResponse.put("message", e.getMessage());
    // errorResponse.put("path", "/leases");
    // return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }

    // @GetMapping("/bybranch")
    // public ResponseEntity<Map<String, Object>> getAllLeases(
    // @RequestParam(defaultValue = "1") int page,
    // @RequestParam int size,
    // @RequestParam(required = false) String sortBy,
    // @RequestParam(required = false, defaultValue = "asc") String sortOrder) {
    // try {
    // Page<Lease> leases = leaseService.getAllLeasesSortedByBranchName(page, size,
    // "asc");
    // Map<String, Object> response = new HashMap<>();
    // response.put("leases", leases.getContent());
    // response.put("currentPage", leases.getNumber() + 1);
    // response.put("totalItems", leases.getTotalElements());
    // response.put("totalPages", leases.getTotalPages());
    // return new ResponseEntity<>(response, HttpStatus.OK);
    // } catch (Exception e) {
    // Map<String, Object> errorResponse = new HashMap<>();
    // errorResponse.put("timestamp", LocalDateTime.now());
    // errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    // errorResponse.put("error", "Internal Server Error");
    // errorResponse.put("message", e.getMessage());
    // errorResponse.put("path", "/leases");
    // return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    // }
    // }

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
    public ResponseEntity<Map<String, Object>> getExpiredLeases(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam int size,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        try {
            Map<String, Object> response = new HashMap<>();
            if (startYear != null && endYear != null) {
                // Filter between two dates
                response = leaseService.getExpiredLeasesByContractYearRange(startYear, endYear, page, size, sortBy,
                        sortOrder);
            } else if (startYear != null && endYear == null) {
                // Filter with only start date
                response = leaseService.getExpiredLeasesByContractYear(startYear, DEFAULT_END_YEAR,
                        page, size, sortBy, sortOrder);
            } else if (startYear == null && endYear != null) {
                // Filter with only end date
                response = leaseService.getExpiredLeasesByContractYear(DEFAULT_START_YEAR, endYear,
                        page, size, sortBy, sortOrder);
            } else if (sortBy != null && sortOrder != null) {
                if (sortOrder != null && sortBy.equals("branchName")) {
                    response = leaseService.getAllExpiredLeasesSortedByBranchName(page, size,
                            sortOrder);
                } else {
                    response = leaseService.getAllExpiredLeasesSorted(page, size, sortBy, sortOrder);
                }
                // filter with other lease properties like contract start date or contract
                // registeration date etc
            } else {
                // No date filter, get all expired leases
                response = leaseService.getAllExpiredLeases(page, size);

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
        // return leaseService.getAllExpiredLeases(page, size);
    }

    @GetMapping("/activeContracts")
    public ResponseEntity<Map<String, Object>> getAllActiveLeases(

            @RequestParam(defaultValue = "1") int page,
            @RequestParam int size,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {
        try {
            Map<String, Object> response = new HashMap<>();
            // filter with other lease properties like contract start date or contract
            // registeration date etc
            if (startYear != null && endYear != null) {
                // Filter between two dates
                response = leaseService.getActiveLeasesByContractYearRange(startYear, endYear, page, size, sortBy,
                        sortOrder);
            } else if (startYear != null && endYear == null) {
                // Filter with only start date
                response = leaseService.getActiveLeasesByContractYear(startYear, DEFAULT_END_YEAR,
                        page, size, sortBy, sortOrder);
            } else if (startYear == null && endYear != null) {
                // Filter with only end date
                response = leaseService.getActiveLeasesByContractYear(DEFAULT_START_YEAR, endYear,
                        page, size, sortBy, sortOrder);
            } else if (sortBy != null && sortOrder != null) {
                if (sortOrder != null && sortBy.equals("branchName")) {
                    response = leaseService.getAllActiveLeasesSortedByBranchName(page, size,
                            sortOrder);
                } else {
                    response = leaseService.getAllActiveLeasesSorted(page, size, sortBy, sortOrder);
                }

            } else {
                // No date filter, get all expired leases
                response = leaseService.getAllActiveLeases(page, size);
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
        // return leaseService.getAllActiveLeases(page, size);
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
            @RequestParam(required = false, defaultValue = "-1") int selectedMonth,
            @RequestParam(required = false) LocalDate date) {
        return leaseService
                .generateReportsForAll(requestBody.getType(), requestBody.getTerm(), selectedYear, selectedMonth, date)
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

    @PostMapping("/files")
    public ResponseEntity<?> uploadImages(@RequestPart("file") MultipartFile imageFile) {
        // Assuming you have a storage folder path defined in your properties
        String storageFolderPath = uploadDir;

        try {
            // Get the original filename
            String fileNameVariable = imageFile.getOriginalFilename();

            // Check if the file is a PDF
            if (!fileNameVariable.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "Only PDF files are allowed"));
            }

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
        Map<String, Object> response = new HashMap<>();

        if (startYear != 0) {
            List<Lease> leases = leaseService.getLeasesByBranchIdAndContractRegistrationYear(branchId, startYear);
            if (leases.isEmpty()) {
                response.put("message", "No leases found for branch ID: " + branchId + " in the year " + startYear);
                return new ResponseEntity<Object>(response, HttpStatus.NOT_FOUND);
            } else {
                response.put("leases", mapLeasesToResponse(leases));
                // response.put("leases", leases); // Assuming leases is a list of Lease objects
            }
        } else {

            List<Lease> leases = leaseService.findLeasesByBranchId(branchId);
            response.put("leases", mapLeasesToResponse(leases));
            // response.put("leases", leases); // Assuming leases is a list of Lease objects

        }
        return ResponseEntity.ok(response);

    }

    @GetMapping("/byDistrictId/{districtId}")
    public ResponseEntity<Object> getLeasesByDistrictId(@PathVariable Long districtId,
            @RequestParam(required = false) Integer startYear) {
        List<Lease> leases;

        if (startYear != null) {
            leases = leaseService.findByBranchDistrictDistrictIdAndContractRegisteredDateYear(districtId, startYear);
        } else {
            leases = leaseService.findByDistrictId(districtId);
        }

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

    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        // Get the file path
        Path filePath = getFilePath(filename);

        // Check if the file exists
        if (filePath == null || !filePath.toFile().exists()) {
            // Handle the case when the file does not exist
            return ResponseEntity.notFound().build();
        }

        try {
            // Load file as Resource
            Resource resource = new org.springframework.core.io.PathResource(filePath);

            // Set Content-Disposition header to force download
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" +
                    resource.getFilename());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            // Handle exceptions appropriately (e.g., log and return an error response)
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    
    @GetMapping("/filess/{filename:.+}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filename) {
        // Get the file path
        Path filePath = getFilePath(filename);

        // Check if the file exists
        if (filePath == null || !filePath.toFile().exists()) {
            // Handle the case when the file does not exist
            return ResponseEntity.notFound().build();
        }

        try {
            // Load file as Resource
            Resource resource = new org.springframework.core.io.PathResource(filePath);

            // Set Content-Disposition header to suggest inline display
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + resource.getFilename());

            // Determine the Content-Type based on the file extension
            String contentType = URLConnection.guessContentTypeFromName(filename);

            // If the Content-Type cannot be determined, set a default
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Set Content-Type header
            headers.setContentType(MediaType.parseMediaType(contentType));

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(resource.contentLength())
                    .body(resource);
        } catch (IOException e) {
            // Handle exceptions appropriately (e.g., log and return an error response)
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    private Path getFilePath(String filename) {
        // Implement this method based on your actual logic
        // It should return the Path to the file or handle non-existent files
        // appropriately
        // For now, let's assume the files are stored in the uploadDir
        String uploadDir = storageFolderPath;
        return Paths.get(uploadDir, filename);
    }

    @PostMapping("/reports/byDistrict/{districtId}")
    public String generateReportsForDistricts(
            @PathVariable Long districtId,
            @RequestBody GenerateReportsRequestBody requestBody,
            @RequestParam(required = false, defaultValue = "-1") int selectedYear,
            @RequestParam(required = false, defaultValue = "-1") int selectedMonth) {
        String DistrictSummary = "";
        if (selectedYear != -1) {
            DistrictSummary = leaseService.generateReportsForDistrictForSelectedYear(requestBody.getType(),
                    requestBody.getTerm(),
                    selectedYear, selectedMonth,
                    districtId).toString();
        } else {
            DistrictSummary = leaseService.generateReportsForDistrict(requestBody.getType(),
                    requestBody.getTerm(),
                    selectedYear, selectedMonth,
                    districtId).toString();
        }
        return DistrictSummary;
    }

}
