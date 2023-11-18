package com.example.demo.lease.Controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.lease.GenerateReportsRequestBody;
import com.example.demo.lease.Model.Lease;
import com.example.demo.lease.Repository.LeaseRepository;
import com.example.demo.lease.Service.LeaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@CrossOrigin
@RestController
@RequestMapping("/leases")
public class LeaseController {

    private final LeaseService leaseService;

    @Autowired
    private LeaseRepository leaseRepository;

    public LeaseController(LeaseService leaseService) {
        this.leaseService = leaseService;
    }

    // @GetMapping("/byBranchId/{branchId}")
    // public List<Lease> getLeasesByBranchId(@PathVariable Long branchId) {
    // return leaseRepository.findByBranchId(branchId);
    // }
    @GetMapping("/byBranchId/{branchId}")
    public List<Lease> getLeasesByBranchId(@PathVariable Long branchId) {
        List<Lease> leases = leaseService.getLeasesByBranchId(branchId);

        // Ensure that the installmentDetails field is correctly formatted as JSON
        for (Lease lease : leases) {
            String installmentDetails = lease.getInstallmentDetails(); // Retrieve the JSON string from the entity
            if (installmentDetails != null) {
                // Handle JSON parsing to a JSONObject here if necessary
                // For example, if the installmentDetails is in a specific format
                // JSONObject installmentDetailsJson = new JSONObject(installmentDetails);
                // lease.setInstallmentDetails(installmentDetailsJson); // If you need it as a
                // JSONObject

                // Or, if you don't need to parse it and just return it as a JSON string:
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
    public ResponseEntity<List<Lease>> getAllLeases() {
        List<Lease> leases = leaseService.getAllLeases();
        return new ResponseEntity<>(leases, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lease> getLeaseById(@PathVariable Long id) {
        Lease lease = leaseService.getLeaseById(id);
        if (lease != null) {
            return new ResponseEntity<>(lease, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/searchContract/{contractNumber}")
    public ResponseEntity<Lease> getLeaseByContractNumber(@PathVariable String contractNumber) {
        Lease lease = leaseService.getLeaseByContractNumber(contractNumber);
        if (lease != null) {
            return new ResponseEntity<>(lease, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<Lease> addNewLease(@RequestBody Lease lease) throws Exception {
        try {
            Lease createdLease = leaseService.addNewLease(lease);
            return new ResponseEntity<>(createdLease, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    // @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    // @ApiOperation("Add a new lease")
    // public ResponseEntity<Lease> addNewLease(
    // @RequestBody Lease lease,
    // @RequestPart("file") MultipartFile file) throws Exception {
    // try {
    // Lease createdLease = leaseService.addNewLease(lease, file);
    // return new ResponseEntity<>(createdLease, HttpStatus.CREATED);
    // } catch (IllegalArgumentException e) {
    // return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    // }
    // }

    // @PostMapping
    // public ResponseEntity<Lease> addNewLease(@RequestParam("file") MultipartFile
    // file,
    // @RequestParam("lease") String leaseJson) throws
    // Exception {
    // try {
    // ObjectMapper objectMapper = new ObjectMapper();
    // Lease lease = objectMapper.readValue(leaseJson, Lease.class);
    // Lease createdLease = leaseService.addNewLease(lease, file);
    // return new ResponseEntity<>(createdLease, HttpStatus.CREATED);
    // } catch (IllegalArgumentException e) {
    // return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    // }
    // }

    // @PostMapping
    // public ResponseEntity<Lease> addNewLease(
    // @RequestParam("file") MultipartFile file,
    // @RequestBody Lease lease) throws Exception {
    // try {
    // Lease createdLease = leaseService.addNewLease(lease, file);
    // return new ResponseEntity<>(createdLease, HttpStatus.CREATED);
    // } catch (IllegalArgumentException e) {
    // return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    // }
    // }

    @GetMapping("/unauthorized")
    public List<Lease> getUnauthorizedLeases() {
        return leaseService.getUnauthorizedLeases();
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

        Map<String, Long> contractInfo = new HashMap<>();
        contractInfo.put("totalContracts", totalContracts);
        contractInfo.put("totalUnexpiredContracts", totalUnexpiredContracts);
        contractInfo.put("totalExpiredContracts", totalExpiredContracts);
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

    // @Autowired
    // private FileStorageService fileStorageService;

    // @Autowired
    // private FileEntityRepository fileEntityRepository;

    // @PutMapping("/{id}/uploadFile")
    // public ResponseEntity<String> uploadFile(
    // @RequestParam("file") MultipartFile file, Model model, Lease lease) {

    // if (leaseRepository.existsById(lease.getId())) {
    // try {
    // String filePath = fileStorageService.storeFile(file);

    // // Save file information in the database

    // FileEntity fileEntity = new FileEntity();
    // fileEntity.setFileName(file.getOriginalFilename());
    // fileEntity.setFileType(file.getContentType());
    // fileEntity.setFilePath(filePath);
    // fileEntityRepository.save(fileEntity);

    // return new ResponseEntity<>(HttpStatus.OK);
    // } catch (IOException e) {
    // return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    // }
    // }
    // return null;
    // }

    // @PutMapping("/{id}/updateWithFile")
    // public ResponseEntity<Lease> updateLeaseWithFile(@PathVariable Long id,
    // @RequestParam("file") MultipartFile file) {
    // try {

    // Lease updatedLease = leaseService.updateLeaseWithFile(id, file);
    // return new ResponseEntity<>(updatedLease, HttpStatus.OK);
    // } catch (IOException | NotFoundException e) {
    // return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    // }
    // }

}
