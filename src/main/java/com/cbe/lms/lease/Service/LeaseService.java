package com.cbe.lms.lease.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cbe.lms.lease.Model.Lease;
import com.cbe.lms.lease.Repository.LeaseRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LeaseService {

    private final LeaseRepository leaseRepository;
    private final CalculateReport calculateReport;
    // private final BranchRepository branchRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    private final LocalDate currentDate = LocalDate.now();

    public LeaseService(LeaseRepository leaseRepository, CalculateReport calculateReport) {
        this.leaseRepository = leaseRepository;
        this.calculateReport = calculateReport;
        // this.branchRepository = branchRepository;

    }

    public List<Lease> getAllLeases() {
        return leaseRepository.findAll();
    }

    public List<Lease> getLeasesByBranchId(Long branchId) {
        return leaseRepository.findByBranchId(branchId);
    }

    public Map<String, Object> getLeaseById(Long id) {
        Optional<Lease> optionalLease = leaseRepository.findById(id);

        if (optionalLease.isPresent()) {
            Lease lease = optionalLease.get();
            Map<String, Object> leaseData = new HashMap<>();
            leaseData.put("id", lease.getId());
            leaseData.put("discountRate", lease.getDiscountRate());
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
            leaseData.put("contractReason", lease.getContractReason());
            leaseData.put("fileName", lease.getFileName());

            if (lease.getBranch() != null) {
                leaseData.put("branchId", lease.getBranch().getBranchId());
                leaseData.put("branchName", lease.getBranch().getBranchName());
                // Include other branch fields

            }

            return leaseData;
        }

        return null; // or throw an exception if the lease is not found
                     // leaseRepository.findById(id).orElse(null);
    }

    public List<Lease> getUnauthorizedLeases() {
        return leaseRepository.findAllByAuthorizationFalse();
    }

    public Map<String, Object> getAllExpiredLeases(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Lease> leases = leaseRepository.findByContractEndDateBeforeAndAuthorizationIsTrue(currentDate, pageable);
        // LocalDate currentDate = LocalDate.of(2026, 1, 1);
        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leases.getTotalElements()));

        response.put("leases",
                leases.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getAllActiveLeases(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Lease> leases = leaseRepository.findByContractEndDateAfterAndAuthorizationIsTrue(currentDate, pageable);
        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leases.getTotalElements()));

        response.put("leases",
                leases.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public void authorizeLeaseById(Long leaseId) throws NotFoundException {
        Lease lease = leaseRepository.findById(leaseId)
                .orElseThrow(() -> new NotFoundException());

        lease.setAuthorization(true);
        leaseRepository.save(lease);
    }

    public Lease addNewLease(Lease lease) throws Exception {
        // lease.setContractRegisteredDate(LocalDate.now());
        lease.setAuthorization(true);
        return leaseRepository.save(lease);
    }

    public Lease updateLease(Long id, Lease updatedLease) {
        Optional<Lease> existingLeaseOptional = leaseRepository.findById(id);

        if (existingLeaseOptional.isPresent()) {
            Lease existingLease = existingLeaseOptional.get();

            // Use BeanUtils to selectively copy non-null properties from updatedLease to
            // existingLease
            BeanUtils.copyProperties(updatedLease, existingLease, getNullPropertyNames(updatedLease));

            // Save the updated lease
            return leaseRepository.save(existingLease);
        }

        return null; // or throw NotFoundException
    }

    // Helper method to get null property names from an object
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            // Check if value of this property is null, then add it to emptyNames
            if (src.getPropertyValue(pd.getName()) == null) {
                emptyNames.add(pd.getName());
            }
        }

        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
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

    private JSONObject generateReportObject(String type, String term, int selectedYear, int selectedMonth,
            Lease report) {
        CalculateReport calculate = new CalculateReport();
        long id = report.getId();
        LocalDate startDate = report.getContractStartDate();
        LocalDate endDate = report.getContractEndDate();
        LocalDate contractStarDate = report.getContractStartDate();
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
        String branchName = report.getBranch().getBranchName();

        String contractType = report.getContractType();
        Double leaseLiablity = calculate.calculateLeaseLiability(totalPayment, advancePayment, discountRate, startDate,
                endDate, installmentDetails, contractRegisteredDate);
        Double rightOfUse = calculate.calculateRightOfUseAsset(advancePayment, leaseLiablity, leaseIncentive,
                initialDirectCost, installmentDetails, contractRegisteredDate);
        Double depreciationPerMonth = calculate.calculateDepreciationPerMonth(rightOfUse,
                CalculateReport.monthBetween(startDate, endDate));
        Long branchId = getBranchIdForLease(id);
        if (branchId == null) {
            // Handle the case where branchId is null, throw an exception, log an error, or
            // handle it accordingly
            throw new IllegalArgumentException("BranchId is null for leaseId: " + id);
        }
        // Perform the report calculation based on the provided code
        String reportResult = "";
        if (type.equals("multiple")) {
            // reportResult = CalculateReport.calculateReportGM(id, startDate, endDate, rightOfUse, depreciationPerMonth,
            //         term, totalPayment, advancePayment, discountRate, leaseLiablity, selectedYear, selectedMonth,
            //         contractRegisteredDate, branchId, contractType);
        } else if (type.equals("single") && term.equals("monthly")) {
            reportResult = CalculateReport.calculateReportM(id, startDate, endDate, rightOfUse, depreciationPerMonth,
                    term, totalPayment, advancePayment, discountRate, leaseLiablity, contractRegisteredDate,
                    installmentDetails, branchId, contractType, endDate, startDate, branchName);
        } else if (type.equals("single") && term.equals("yearly")) {
            reportResult = CalculateReport.calculateReportY(id, startDate, endDate, rightOfUse, depreciationPerMonth,
                    term, totalPayment, advancePayment, discountRate, leaseLiablity, contractRegisteredDate,
                    installmentDetails, branchId, contractType, contractStarDate, endDate, branchName);
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

    public List<JSONObject> generateReportsForAll(String type, String term, int selectedYear, int selectedMonth,
            LocalDate date) {
        List<JSONObject> reports = new ArrayList<>();
        List<Lease> allLeases;

        if (date == null) {
            allLeases = leaseRepository.findAllByAuthorizationTrue();
        } else {
            allLeases = leaseRepository.findAllByAuthorizationTrueAndContractRegisteredDate(date);
        }
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

    public Long getBranchIdForLease(Long leaseId) {
        Optional<Lease> optionalLease = leaseRepository.findById(leaseId);

        if (optionalLease.isPresent()) {
            Lease lease = optionalLease.get();
            if (lease.getBranch() != null) {
                return lease.getBranch().getBranchId();

            }
        }

        // Return null or throw an exception if branchId is not available
        return null; // or throw new NotFoundException("BranchId not found for leaseId: " + leaseId);
    }

    public Map<String, Object> getAllLeases(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size); // Adjust the page number
        Page<Lease> leasePage = leaseRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size,
                leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getAllLeasesSorted(int page, int size, String sortBy,
            String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        PageRequest pageable = PageRequest
                .of(page - 1, size, Sort.by(direction, sortBy));

        Page<Lease> leasePage = leaseRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size,
                leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getAllExpiredLeasesSorted(int page, int size, String sortBy,
            String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;

        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        PageRequest pageable = PageRequest
                .of(page - 1, size, Sort.by(direction, sortBy));

        Page<Lease> leasePage = leaseRepository.findByContractEndDateBeforeAndAuthorizationIsTrue(currentDate,
                pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size,
                leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getAllActiveLeasesSorted(int page, int size, String sortBy,
            String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;

        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        PageRequest pageable = PageRequest
                .of(page - 1, size, Sort.by(direction, sortBy));

        Page<Lease> leasePage = leaseRepository.findByContractEndDateAfterAndAuthorizationIsTrue(currentDate,
                pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size,
                leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getAllLeasesSortedByBranchName(int page, int size, String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        PageRequest pageable = PageRequest
                .of(page - 1, size, Sort.by(direction, "branch.branchName"));
        Page<Lease> leasePage = leaseRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size,
                leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getAllExpiredLeasesSortedByBranchName(int page, int size, String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        PageRequest pageable = PageRequest
                .of(page - 1, size, Sort.by(direction, "branch.branchName"));
        Page<Lease> leasePage = leaseRepository.findByContractEndDateBeforeAndAuthorizationIsTrue(currentDate,
                pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size,
                leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getAllActiveLeasesSortedByBranchName(int page, int size, String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        PageRequest pageable = PageRequest
                .of(page - 1, size, Sort.by(direction, "branch.branchName"));
        Page<Lease> leasePage = leaseRepository.findByContractEndDateAfterAndAuthorizationIsTrue(currentDate,
                pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size,
                leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public long getTotalLeasesCount() {
        return leaseRepository.count();
    }

    private Map<String, Object> mapLeaseWithBranchId(Lease lease) {

        Double leaseLiability = calculateLeaseLiabilityWrapper(lease.getTotalPayment().doubleValue(),
                lease.getAdvancePayment().doubleValue(), lease.getDiscountRate(),
                lease.getContractStartDate(),
                lease.getContractEndDate(), lease.getInstallmentDetails(),
                lease.getContractRegisteredDate());

        lease.setLeaseLiability(leaseLiability);

        Map<String, Object> leaseData = new HashMap<>();
        leaseData.put("id", lease.getId());
        leaseData.put("discountRate", lease.getDiscountRate());
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
        leaseData.put("contract Reason", lease.getContractReason());
        leaseData.put("fileName", lease.getFileName());
        leaseData.put("leaseLiability", lease.getLeaseLiability());

        // Include other lease fields

        if (lease.getBranch() != null) {
            leaseData.put("branchId", lease.getBranch().getBranchId());
            leaseData.put("branchName", lease.getBranch().getBranchName());
            // Include other branch fields
        }

        return leaseData;
    }

    public double calculateLeaseLiabilityWrapper(Double totalContractPrice, double advancePayment, double discountRate,
            LocalDate contractStartDate, LocalDate contractEndDate, String installmentDetails,
            LocalDate contractRegisteredDate) {
        // You can now call the method from CalculateReport
        return calculateReport.calculateLeaseLiability(totalContractPrice, advancePayment, discountRate,
                contractStartDate, contractEndDate, installmentDetails, contractRegisteredDate);
    }

    public Map<String, Object> getLeasesByContractYear(int startYear, int endYear, int page, int size, String sortBy,
            String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;
        PageRequest pageable;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        if (sortBy != null || sortOrder != null) {
            if (sortBy.equals("branchName")) {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, "branch.branchName"));
            } else {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, sortBy));
            }
        } else {
            pageable = PageRequest
                    .of(page - 1, size);
        }
        Page<Lease> leasePage = leaseRepository.findByContractStartDateYearORContractEndDateYear(startYear, endYear,
                pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getExpiredLeasesByContractYear(int startYear, int endYear, int page, int size,
            String sortBy, String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;
        PageRequest pageable;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        if (sortBy != null || sortOrder != null) {
            if (sortBy.equals("branchName")) {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, "branch.branchName"));
            } else {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, sortBy));
            }
        } else {
            pageable = PageRequest
                    .of(page - 1, size);
        }
        Page<Lease> leasePage = leaseRepository.findByContractStartDateYearORContractEndDateYearAndBeforeEndDate(
                startYear, endYear, currentDate,
                pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getActiveLeasesByContractYear(int startYear, int endYear, int page, int size,
            String sortBy, String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;
        PageRequest pageable;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        if (sortBy != null || sortOrder != null) {
            if (sortBy.equals("branchName")) {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, "branch.branchName"));
            } else {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, sortBy));
            }

        } else {
            pageable = PageRequest
                    .of(page - 1, size);
        }

        Page<Lease> leasePage = leaseRepository.findByContractStartDateYearORContractEndDateYearAndContractEndDateAfter(
                startYear, endYear, currentDate,
                pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getLeasesByContractYearRange(int startYear, int endYear, int page, int size,
            String sortBy, String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;
        PageRequest pageable;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        if (sortBy != null || sortOrder != null) {
            if (sortBy.equals("branchName")) {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, "branch.branchName"));
            } else {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, sortBy));
            }
        } else {
            pageable = PageRequest
                    .of(page - 1, size);
        }
        Page<Lease> leasePage = leaseRepository.findByContractStartDateYearAndContractEndDateYear(startYear, endYear,
                pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getExpiredLeasesByContractYearRange(int startYear, int endYear, int page, int size,
            String sortBy, String sortOrder) {
        Sort.Direction direction = Sort.Direction.ASC;
        PageRequest pageable;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        if (sortBy != null || sortOrder != null) {
            if (sortBy.equals("branchName")) {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, "branch.branchName"));
            } else {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, sortBy));
            }
        } else {
            pageable = PageRequest
                    .of(page - 1, size);
        }
        Page<Lease> leasePage = leaseRepository.findByContractStartDateYearAndContractEndDateYearAndBeforeEndDate(
                startYear, endYear, currentDate, pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getActiveLeasesByContractYearRange(int startYear, int endYear, int page, int size,
            String sortBy, String sortOrder) {

        Sort.Direction direction = Sort.Direction.ASC;
        PageRequest pageable;
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        if (sortBy != null || sortOrder != null) {
            if (sortBy.equals("branchName")) {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, "branch.branchName"));
            } else {
                pageable = PageRequest
                        .of(page - 1, size, Sort.by(direction, sortBy));
            }

        } else {
            pageable = PageRequest
                    .of(page - 1, size);
        }
        Page<Lease> leasePage = leaseRepository
                .findByContractStartDateYearAndContractEndDateYearAndContractEndDateAfter(
                        startYear, endYear, currentDate, pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public List<Lease> getLeasesByBranchIdAndContractRegistrationYear(Long branchId, int contractRegistrationYear) {
        return leaseRepository.findByBranchIdAndContractRegisteredDateYear(branchId, contractRegistrationYear);
    }

    public List<Lease> findByBranchDistrictDistrictIdAndContractRegisteredDateYear(Long districtId,
            Integer contractRegisteredDate) {
        return leaseRepository.findByBranchDistrictDistrictIdAndContractRegisteredDateYear(districtId,
                contractRegisteredDate);
    }

    public List<Lease> findByDistrictId(Long districtId) {
        return leaseRepository.findByDistrictIdQuery(districtId);
    }

    public List<Lease> findLeasesByBranchId(Long branchId) {
        return leaseRepository.findLeasesByBranchId(branchId);
    }

    public JSONArray generateReportsForDistrictForSelectedYear(String type, String term, int selectedYear,
            int selectedMonth,
            Long districtId) {
        JSONArray reportsArray = new JSONArray();

        List<Lease> leasesInDistrict = leaseRepository.findByBranchDistrictDistrictIdAndContractRegisteredDateYear(
                districtId,
                selectedYear);

        for (Lease lease : leasesInDistrict) {
            JSONObject reportObject = generateReportObject(type, term, selectedYear, selectedMonth, lease);
            reportsArray.put(reportObject);
        }

        return reportsArray;
    }

    public JSONArray generateReportsForDistrict(String type, String term, int selectedYear, int selectedMonth,
            Long districtId) {
        List<Lease> leasesInDistrict = leaseRepository.findByDistrictIdQuery(districtId);

        JSONArray reportsArray = new JSONArray();

        for (Lease lease : leasesInDistrict) {
            JSONObject reportObject = generateReportObject(type, term, selectedYear, selectedMonth, lease);
            // System.out.println(lease.getBranch().getDistrict().ge);
            reportsArray.put(reportObject);
        }

        return reportsArray;
    }

}
