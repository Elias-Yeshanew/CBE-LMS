package com.example.demo.lease.Model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonRawValue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lease")
public class Lease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_number", unique = true, nullable = false)
    private String contractNumber;

    @Column(name = "discount_rate", nullable = false)
    private Double discountRate;

    @Column(name = "contract_start_date", nullable = false)
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date", nullable = false)
    private LocalDate contractEndDate;

    @Column(name = "total_payment", nullable = false)
    private BigDecimal totalPayment;

    @Column(name = "advance_payment")
    private BigDecimal advancePayment;

    @Column(name = "initial_direct_cost")
    private BigDecimal initialDirectCost;

    @Column(name = "lease_incentive")
    private BigDecimal leaseIncentive;

    @Column(name = "number_of_installments")
    private int numberOfInstallments;

    @Column(name = "authorization", nullable = false)
    private boolean authorization;

    @JsonRawValue
    @Column(name = "installment_details")
    private String installmentDetails;

    @Column(name = "contractRegisteredDate", nullable = false)
    private LocalDate contractRegisteredDate;

    @Column(name = "contract_type")
    private String contractType;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "branch_id", referencedColumnName = "branch_id")
    private Branch branch;

    // @JsonSerialize(using = FileEntitySerializer.class)
    // @OneToOne(cascade = CascadeType.ALL)
    // @JoinColumn(name = "file_Id", referencedColumnName = "file_Id")
    // private FileEntity fileEntity;

    public Lease(Long id, String contractNumber, Double discountRate, LocalDate contractStartDate,
            LocalDate contractEndDate, BigDecimal totalPayment, BigDecimal advancePayment, BigDecimal initialDirectCost,
            BigDecimal leaseIncentive, int numberOfInstallments, boolean authorization, LocalDate advancePaymentDate,
            String installmentDetails, LocalDate contractRegisteredDate,
            String contractType) {
        this.id = id;
        this.totalPayment = totalPayment;
        this.advancePayment = advancePayment;
        this.contractNumber = contractNumber;
        this.contractStartDate = contractStartDate;
        this.contractEndDate = contractEndDate;
        this.discountRate = discountRate;
        this.initialDirectCost = initialDirectCost;
        this.leaseIncentive = leaseIncentive;
        this.numberOfInstallments = numberOfInstallments;
        this.authorization = authorization;
        this.installmentDetails = installmentDetails;
        this.contractRegisteredDate = contractRegisteredDate;
        this.contractType = contractType;

    }

    public Lease() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isAuthorization() {
        return authorization;
    }

    public void setAuthorization(boolean authorization) {
        this.authorization = authorization;
    }

    public boolean getAuthorization() {
        return authorization;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Double discountRate) {
        this.discountRate = discountRate;
    }

    public LocalDate getContractStartDate() {
        return contractStartDate;
    }

    public void setContractStartDate(LocalDate contractStartDate) {
        this.contractStartDate = contractStartDate;
    }

    public LocalDate getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(LocalDate contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public void setTotalPayment(BigDecimal totalPayment) {
        this.totalPayment = totalPayment;
    }

    public BigDecimal getAdvancePayment() {
        return advancePayment;
    }

    public void setAdvancePayment(BigDecimal advancePayment) {
        this.advancePayment = advancePayment;
    }

    public int getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(int numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public void setInitialDirectCost(BigDecimal initialDirectCost) {
        this.initialDirectCost = initialDirectCost;
    }

    public BigDecimal getInitialDirectCost() {
        return initialDirectCost;
    }

    public void setLeaseIncentive(BigDecimal leaseIncentive) {
        this.leaseIncentive = leaseIncentive;
    }

    public BigDecimal getLeaseIncentive() {
        return leaseIncentive;
    }

    public String getInstallmentDetails() {
        return installmentDetails;
    }

    public void setInstallmentDetails(String installmentDetails) {
        this.installmentDetails = installmentDetails;
    }

    public void setInstallmentDetails(JSONObject installmentDetails2) {
    }

    public LocalDate getContractRegisteredDate() {
        return contractRegisteredDate;

    }

    public void setContractRegisteredDate(LocalDate contractRegisteredDate) {
        this.contractRegisteredDate = contractRegisteredDate;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    private double leasePayment;

    private double outstandingBalance;

    public Double getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(Double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public Double getLeasePayment() {
        return leasePayment;
    }

    public void setLeasePayment(Double leasePayment) {
        this.leasePayment = leasePayment;
    }

    // public FileEntity getFileEntity() {
    // return fileEntity;
    // }

    // public void setFileEntity(FileEntity fileEntity) {
    // this.fileEntity = fileEntity;

    // }

    @Column(name = "file_path")
    private String filePath;

    // Existing methods...

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "Lease [id=" + id + ", contractNumber=" + contractNumber + ", discountRate=" + discountRate
                + ", contractStartDate=" + contractStartDate + ", contractEndDate=" + contractEndDate
                + ", totalPayment=" + totalPayment + ", advancePayment=" + advancePayment + ", initialDirectCost="
                + initialDirectCost + ", leaseIncentive=" + leaseIncentive + ", numberOfInstallments="
                + numberOfInstallments + ", authorization=" + authorization + ", installmentDetails="
                + installmentDetails + ", contractRegisteredDate = " + contractRegisteredDate
                + ", contractType = " + contractType + " ]";
    }

}
