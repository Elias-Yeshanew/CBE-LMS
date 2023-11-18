package com.example.demo.lease.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Model.FileEntity;
import com.example.demo.lease.Model.Lease;
import com.example.demo.lease.Repository.LeaseRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class LeaseService {
    private static final String ALPHA_NUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final LeaseRepository leaseRepository;
    // private final BranchRepository branchRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private FileStorageService fileStorageService;

    public LeaseService(LeaseRepository leaseRepository) {
        this.leaseRepository = leaseRepository;
        // this.branchRepository = branchRepository;

    }

    public List<Lease> getAllLeases() {
        return leaseRepository.findAll();
    }

    public List<Lease> getLeasesByBranchId(Long branchId) {
        return leaseRepository.findByBranchId(branchId);
    }

    public Lease getLeaseById(Long id) {
        return leaseRepository.findById(id).orElse(null);
    }

    public Lease getLeaseByContractNumber(String contractNumber) {
        return leaseRepository.findByContractNumber(contractNumber);
    }

    public List<Lease> getUnauthorizedLeases() {
        return leaseRepository.findAllByAuthorizationFalse();
    }

    public void authorizeLeaseById(Long leaseId) throws NotFoundException {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new NotFoundException());

        lease.setAuthorization(true);
        leaseRepository.save(lease);
    }

    public static String generateRandomAlphaNumeric() {
        StringBuilder sb = new StringBuilder(8);
        Random random = new Random();

        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(ALPHA_NUMERIC_CHARS.length());
            char randomChar = ALPHA_NUMERIC_CHARS.charAt(index);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    public Lease addNewLease(Lease lease) throws Exception {
        System.out.println(lease);
        Branch branch = lease.getBranch();
        String add = branch.toString().substring(0, 3);
        lease.setContractNumber(add + generateRandomAlphaNumeric());
        String contractNumber = add + lease.getContractNumber();
        lease.setContractRegisteredDate(LocalDate.now());

        lease.setAuthorization(false);
        if (leaseRepository.existsByContractNumber(contractNumber)) {
            throw new IllegalArgumentException("A lease with contract number " +
                    contractNumber + " already exists.");
        }
        return leaseRepository.save(lease);
    }

    // public Lease addNewLease(Lease lease, MultipartFile file) throws Exception {
    // if (lease == null) {
    // throw new IllegalArgumentException("Lease cannot be null");
    // }

    // // Log lease information
    // String add = lease.getBranch().getBranchName().substring(0, 3);
    // lease.setContractNumber(add + generateRandomAlphaNumeric());
    // String contractNumber = add + lease.getContractNumber();
    // lease.setContractRegisteredDate(LocalDate.now());

    // lease.setAuthorization(false);
    // if (leaseRepository.existsByContractNumber(contractNumber)) {
    // throw new IllegalArgumentException("A lease with contract number " +
    // contractNumber + " already exists.");
    // }

    // // Handle file upload
    // if (file != null) {
    // String filePath = fileStorageService.storeFile(file);
    // lease.setFilePath(filePath); // Set the file path in your Lease entity
    // }

    // return leaseRepository.save(lease);
    // }

    public Lease updateLease(Lease lease) {
        if (leaseRepository.existsById(lease.getId())) {
            return leaseRepository.save(lease);
        }
        return null;
    }

    public JSONObject deleteLease(Long id) {
        Optional<Lease> leaseOptional = leaseRepository.findById(id);
        JSONObject response = new JSONObject();

        if (leaseOptional.isPresent() && leaseOptional.get().getAuthorization() == false) {
            leaseRepository.deleteById(id);
            response.put("message", "Lease deleted successfully.");
        } else if (leaseOptional.isPresent() && leaseOptional.get().getAuthorization() == true) {
            response.put("message", "Sorry, authorized contract can't be deleted. Please contact the administrator.");
        } else if (leaseOptional.isEmpty()) {
            response.put("message", "Sorry, lease not found.");
        } else {
            throw new IllegalArgumentException("Invalid lease ID.");
        }

        return response;
    }

    public JSONArray generateReports(String type, String term, int selectedYear, int selectedMonth, List<Integer> ids) {
        JSONArray reportsArray = new JSONArray();

        for (Integer id : ids) {
            Lease report = leaseRepository.findById(id);

            if (report != null) {
                JSONObject reportObject = generateReportObject(type, term, selectedYear, selectedMonth, report);
                reportsArray.put(reportObject);
            }
        }

        return reportsArray;
    }

    static double monthBetweenn(LocalDate startDate, LocalDate endDate) {
        double averageDaysInMonth = 365.25 / 12; // Approximate average number of
        return ChronoUnit.DAYS.between(startDate, endDate) / averageDaysInMonth;
        // return ChronoUnit.MONTHS.between(startDate, endDate);
        // double averageDaysInMonth = 365.25 / 12; // Approximate average number of
        // days in a month
        // return ChronoUnit.DAYS.between(startDate, endDate) / averageDaysInMonth;
    }

    private JSONObject generateReportObject(String type, String term, int selectedYear, int selectedMonth,
            Lease report) {
        CalculateReport calculate = new CalculateReport();
        long id = report.getId();
        LocalDate startDate = report.getContractStartDate();
        LocalDate endDate = report.getContractEndDate();
        BigDecimal advancePaymentB = report.getAdvancePayment();
        Double advancePayment = advancePaymentB.doubleValue();
        Double discountRate = report.getDiscountRate();
        BigDecimal totalPaymnetB = report.getTotalPayment();
        Double totalPayment = totalPaymnetB.doubleValue();
        BigDecimal leaseIncentiveB = report.getLeaseIncentive();
        BigDecimal initialDirectCostB = report.getInitialDirectCost();
        Double leaseIncentive = leaseIncentiveB.doubleValue();
        String installmentDetails = report.getInstallmentDetails();
        Double initialDirectCost = initialDirectCostB.doubleValue();
        LocalDate contractRegisteredDate = report.getContractRegisteredDate();
        double leasePayment = report.getLeasePayment();
        double outstandingBalance = report.getOutstandingBalance();
        String contractType = report.getContractType();
        Double leaseLiablity = calculate.calculateLeaseLiability(totalPayment, advancePayment, discountRate, startDate,
                endDate, installmentDetails);
        Double rightOfUse = calculate.calculateRightOfUseAsset(advancePayment, leaseLiablity, leaseIncentive,
                initialDirectCost);
        Double depreciationPerMonth = calculate.calculateDepreciationPerMonth(rightOfUse,
                monthBetweenn(startDate, endDate));
        Long branchId = getBranchIdForLease(id);
        if (branchId == null) {
            // Handle the case where branchId is null, throw an exception, log an error, or
            // handle it accordingly
            throw new IllegalArgumentException("BranchId is null for leaseId: " + id);
        }
        // Perform the report calculation based on the provided code
        String reportResult = "";
        if (type.equals("multiple")) {
            reportResult = CalculateReport.calculateReportGM(id, startDate, endDate, rightOfUse, depreciationPerMonth,
                    term, totalPayment, advancePayment, discountRate, leaseLiablity, selectedYear, selectedMonth,
                    contractRegisteredDate, branchId, contractType);
        } else if (type.equals("single") && term.equals("monthly")) {
            reportResult = CalculateReport.calculateReportM(id, startDate, endDate, rightOfUse, depreciationPerMonth,
                    term, totalPayment, advancePayment, discountRate, leaseLiablity, contractRegisteredDate,
                    installmentDetails, branchId, contractType);
        } else if (type.equals("single") && term.equals("yearly")) {
            reportResult = CalculateReport.calculateReportY(id, startDate, endDate, rightOfUse, depreciationPerMonth,
                    term, totalPayment, advancePayment, discountRate, leaseLiablity, contractRegisteredDate,
                    installmentDetails, branchId, contractType);
        }

        return new JSONObject(reportResult);
    }

    public long getTotalContracts() {
        return leaseRepository.countByAuthorization(true);
    }

    public long getTotalUnexpiredContracts() {
        long count = leaseRepository.countByContractEndDateAfterAndAuthorization(LocalDate.now(), true);
        return count;
    }

    public long getTotalExpiredContracts() {
        long count = leaseRepository.countByContractEndDateBeforeAndAuthorization(LocalDate.now(), true);
        return count;
    }

    // Deserialize JSON data when reading from the database
    public Lease getLeaseWithInstallmentDetails(Long id) {
        Optional<Lease> optionalLease = leaseRepository.findById(id);
        Lease lease = optionalLease.orElse(null);

        if (lease != null && lease.getInstallmentDetails() != null) {
            try {
                // Deserialize the JSON string to a JSONObject
                JSONObject installmentDetails = new JSONObject(lease.getInstallmentDetails());
                lease.setInstallmentDetails(installmentDetails);
            } catch (JSONException e) {
                e.printStackTrace();
                // Handle JSON parsing exception
            }
        }

        return lease;
    }

    // Serialize JSON data when saving to the database
    public Lease saveLeaseWithInstallmentDetails(Lease lease) throws JsonProcessingException {
        // Serialize the JSONObject to a JSON string
        if (lease.getInstallmentDetails() != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            String installmentDetailsJson = objectMapper.writeValueAsString(lease.getInstallmentDetails());
            lease.setInstallmentDetails(installmentDetailsJson);

        }

        // Save the Lease entity with serialized JSON data
        return leaseRepository.save(lease);
    }

    // public List<JSONObject> generateReportsForAll(String type, String term, int
    // selectedYear, int selectedMonth) {
    // List<Lease> allLeases = leaseRepository.findAll(); // Fetch all leases
    // List<JSONObject> reports = new ArrayList<>();

    // for (Lease lease : allLeases) {
    // JSONObject reportObject = generateReportObject(type, term, selectedYear,
    // selectedMonth, lease);
    // reports.add(reportObject);
    // }

    // return reports;
    // }

    public List<JSONObject> generateReportsForAll(String type, String term, int selectedYear, int selectedMonth) {
        List<Lease> allLeases = leaseRepository.findAllByAuthorizationTrue(); // Fetch all leases
        List<JSONObject> reports = new ArrayList<>();

        for (Lease lease : allLeases) {
            JSONObject reportObject = generateReportObject(type, term, selectedYear, selectedMonth, lease);
            reports.add(reportObject);
        }

        return reports;
    }

    public String uploadFile(Long leaseId, MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String filePath = uploadDir + "/" + fileName;

        Path destinationPath = Paths.get(filePath);
        Files.copy(file.getInputStream(), destinationPath);

        return filePath;
    }

    // public Lease updateLeaseWithFile(Long leaseId, MultipartFile file) throws
    // IOException, NotFoundException {
    // Optional<Lease> optionalLease = leaseRepository.findById(leaseId);
    // if (optionalLease.isPresent()) {
    // Lease existingLease = optionalLease.get();

    // // Handle file update
    // if (file != null) {
    // String filePath = fileStorageService.storeFile(file);

    // // Update file information in the database
    // FileEntity fileEntity = existingLease.getFileEntity();
    // if (fileEntity == null) {
    // fileEntity = new FileEntity();
    // }
    // fileEntity.setFileName(file.getOriginalFilename());
    // fileEntity.setFileType(file.getContentType());
    // fileEntity.setFilePath(filePath);

    // // Associate the updated fileEntity with the lease
    // existingLease.setFileEntity(fileEntity);
    // }

    // // Update other properties if needed
    // // existingLease.setSomeOtherProperty(...);

    // return leaseRepository.save(existingLease);
    // } else {
    // throw new NotFoundException();
    // }
    // }

    public Long getBranchIdForLease(Long leaseId) {
        Optional<Lease> optionalLease = leaseRepository.findById(leaseId);

        if (optionalLease.isPresent()) {
            Lease lease = optionalLease.get();
            if (lease.getBranch() != null) {
                return lease.getBranch().getId();
            }
        }

        // Return null or throw an exception if branchId is not available
        return null; // or throw new NotFoundException("BranchId not found for leaseId: " + leaseId);
    }

}
