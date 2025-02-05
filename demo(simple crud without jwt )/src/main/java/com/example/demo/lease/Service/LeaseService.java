package com.example.demo.lease.Service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LeaseService {
    private static final String ALPHA_NUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final LeaseRepository leaseRepository;
    // private final BranchRepository branchRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

    // @Autowired
    // private FileStorageService fileStorageService;

    public LeaseService(LeaseRepository leaseRepository) {
        this.leaseRepository = leaseRepository;
        // this.branchRepository = branchRepository;

    }

    public List<Lease> getAllLeases() {
        return leaseRepository.findAll();
    }

    // public List<Lease> getAllLeasesWithBranch() {
    // return leaseRepository.findAllWithBranch();
    // }

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
            leaseData.put("numberOfInstallments", lease.getNumberOfInstallments());
            leaseData.put("authorization", lease.isAuthorization());
            leaseData.put("installmentDetails", lease.getInstallmentDetails());
            leaseData.put("contractRegisteredDate", lease.getContractRegisteredDate());
            leaseData.put("contractType", lease.getContractType());

            if (lease.getBranch() != null) {
                leaseData.put("branchId", lease.getBranch().getId());
                leaseData.put("branchName", lease.getBranch().getBranchName());
                // Include other branch fields

            }

            return leaseData;
        }

        return null; // or throw an exception if the lease is not found
                     // leaseRepository.findById(id).orElse(null);
    }

    // public Lease getLeaseByContractNumber(String contractNumber) {
    // return leaseRepository.findByContractNumber(contractNumber);
    // }

    public List<Lease> getUnauthorizedLeases() {
        return leaseRepository.findAllByAuthorizationFalse();
    }

    public Map<String, Object> getAllExpiredLeases(int page, int size) {
        LocalDate currentDate = LocalDate.now();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Lease> leases = leaseRepository.findByContractEndDateBeforeAndAuthorizationIsTrue(currentDate, pageable);
        // LocalDate currentDate = LocalDate.of(2026, 1, 1);
        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leases.getTotalElements()));

        response.put("leases",
                leases.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }
    // public Map<String, Object> getAllExpiredLeases(int page, int size) {
    // LocalDate currentDate = LocalDate.now();

    // PageRequest pageable = PageRequest.of(page - 1, size); // Adjust the page
    // number
    // Page<Lease> leasePage =
    // leaseRepository.findByContractEndDateBeforeAndAuthorizationIsTrue(currentDate,
    // pageable);

    // Map<String, Object> response = new HashMap<>();

    // response.put("pagination", PaginationUtil.buildPagination(page, size,
    // leasePage.getTotalElements()));

    // response.put("leases",
    // leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

    // return response;

    // }

    // public Map<String, Object> getAllExpiredLeasesWithAdditionalFilter(int page,
    // int size, int registeredYear,
    // int endYear) {
    // LocalDate currentDate = LocalDate.now();
    // PageRequest pageable = PageRequest.of(page - 1, size);

    // // Use the custom query with additional filter
    // Page<Lease> resultPage =
    // leaseRepository.findExpiredLeasesWithAdditionalFilter(currentDate,
    // registeredYear,
    // endYear, pageable);

    // Map<String, Object> response = new HashMap<>();

    // response.put("pagination", PaginationUtil.buildPagination(page, size,
    // resultPage.getTotalElements()));
    // response.put("leases",
    // resultPage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

    // return response;
    // }

    // public Map<String, Object> getAllActiveLeasesWithAdditionalFilter(int page,
    // int size, int registeredYear,
    // int endYear) {
    // LocalDate currentDate = LocalDate.now();
    // PageRequest pageable = PageRequest.of(page - 1, size);

    // // Filter active leases
    // Page<Lease> activeLeasePage =
    // leaseRepository.findByContractEndDateAfterAndAuthorizationIsTrue(currentDate,
    // pageable);

    // // Apply additional filter based on contract registered date and end date
    // List<Lease> filteredLeases = activeLeasePage.getContent().stream()
    // .filter(lease -> lease.getContractRegisteredDate().getYear() ==
    // registeredYear
    // && lease.getContractEndDate().getYear() == endYear)
    // .collect(Collectors.toList());
    // Map<String, Object> response = new HashMap<>();

    // response.put("pagination", PaginationUtil.buildPagination(page, size,
    // activeLeasePage.getTotalElements()));
    // response.put("leases",
    // filteredLeases.stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

    // return response;
    // }

    // public Map<String, Object> getAllActiveLeases(int page, int size) {
    // LocalDate currentDate = LocalDate.now();
    // PageRequest pageable = PageRequest.of(page - 1, size); // Adjust the page
    // number
    // Page<Lease> leasePage =
    // leaseRepository.findByContractEndDateAfterAndAuthorizationIsTrue(currentDate,
    // pageable);

    // Map<String, Object> response = new HashMap<>();

    // response.put("pagination", PaginationUtil.buildPagination(page, size,
    // leasePage.getTotalElements()));

    // response.put("leases",
    // leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

    // return response;
    // }

    public Map<String, Object> getAllActiveLeases(int page, int size) {
        // LocalDate currentDate = LocalDate.now();
        // List<Lease> leases =
        // leaseRepository.findByContractEndDateAfterAndAuthorizationIsTrue(currentDate);
        LocalDate currentDate = LocalDate.now();
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Lease> leases = leaseRepository.findByContractEndDateAfterAndAuthorizationIsTrue(currentDate, pageable);
        // LocalDate currentDate = LocalDate.of(2026, 1, 1);
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
        lease.setContractRegisteredDate(LocalDate.now());
        lease.setAuthorization(true);
        return leaseRepository.save(lease);
    }

    // public Lease updateLease(Lease lease) {
    // if (leaseRepository.existsById(lease.getId())) {
    // return leaseRepository.save(lease);
    // }
    // return null;
    // }

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

    public Map<String, Object> getAllLeases(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size); // Adjust the page number
        Page<Lease> leasePage = leaseRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

    public long getTotalLeasesCount() {
        return leaseRepository.count();
    }

    private Map<String, Object> mapLeaseWithBranchId(Lease lease) {
        Map<String, Object> leaseData = new HashMap<>();
        leaseData.put("id", lease.getId());
        leaseData.put("discountRate", lease.getDiscountRate());
        leaseData.put("contractStartDate", lease.getContractStartDate());
        leaseData.put("contractEndDate", lease.getContractEndDate());
        leaseData.put("totalPayment", lease.getTotalPayment());
        leaseData.put("advancePayment", lease.getAdvancePayment());
        leaseData.put("initialDirectCost", lease.getInitialDirectCost());
        leaseData.put("leaseIncentive", lease.getLeaseIncentive());
        leaseData.put("numberOfInstallments", lease.getNumberOfInstallments());
        leaseData.put("authorization", lease.isAuthorization());
        leaseData.put("installmentDetails", lease.getInstallmentDetails());
        leaseData.put("contractRegisteredDate", lease.getContractRegisteredDate());
        leaseData.put("contractType", lease.getContractType());
        // Include other lease fields

        if (lease.getBranch() != null) {
            leaseData.put("branchId", lease.getBranch().getId());
            leaseData.put("branchName", lease.getBranch().getBranchName());
            // Include other branch fields
        }

        return leaseData;
    }

    public Map<String, Object> getLeasesByContractYearRange(int startYear, int endYear, int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size); // Adjust the page number
        Page<Lease> leasePage = leaseRepository.findByContractStartDateYearAndContractEndDateYear(startYear, endYear,
                pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, leasePage.getTotalElements()));

        response.put("leases",
                leasePage.getContent().stream().map(this::mapLeaseWithBranchId).collect(Collectors.toList()));

        return response;
    }

}
