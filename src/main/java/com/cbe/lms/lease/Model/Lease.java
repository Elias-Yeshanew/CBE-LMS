package com.cbe.lms.lease.Model;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lease")
public class Lease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "discount_rate", nullable = false)
    private Double discountRate;

    @Column(name = "contract_start_date", nullable = false)
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date", nullable = false)
    private LocalDate contractEndDate;

    @Column(name = "total_payment", nullable = false)
    private BigDecimal totalPayment;

    @Transient
    private Double leaseLiability;

    @Column(name = "advance_payment")
    private BigDecimal advancePayment;

    @Column(name = "initial_direct_cost")
    private BigDecimal initialDirectCost;

    @Column(name = "lease_incentive")
    private BigDecimal leaseIncentive;

    @Column(name = "authorization", nullable = false)
    private boolean authorization;

    @JsonRawValue
    @Column(name = "installment_details")
    private String installmentDetails;

    @Column(name = "contractRegisteredDate", nullable = false)
    private LocalDate contractRegisteredDate;

    @Column(name = "contract_type")
    private String contractType;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "contract_reason")
    private String contractReason;

    @ManyToOne
    @JoinColumn(name = "branch_id")
    @JsonBackReference
    private Branch branch;

    public Lease(Long id, Double discountRate, LocalDate contractStartDate,
            LocalDate contractEndDate, BigDecimal totalPayment, BigDecimal advancePayment, BigDecimal initialDirectCost,
            BigDecimal leaseIncentive,  boolean authorization, LocalDate advancePaymentDate,
            String installmentDetails, LocalDate contractRegisteredDate,
            String contractType, Branch branch, String fileName, String contractReason) {
        this.id = id;
        this.totalPayment = totalPayment;
        this.advancePayment = advancePayment;
        this.contractStartDate = contractStartDate;
        this.contractEndDate = contractEndDate;
        this.discountRate = discountRate;
        this.initialDirectCost = initialDirectCost;
        this.leaseIncentive = leaseIncentive;
        this.authorization = authorization;
        this.installmentDetails = installmentDetails;
        this.contractRegisteredDate = contractRegisteredDate;
        this.contractType = contractType;
        this.fileName = fileName;
        this.contractReason = contractReason;

    }

    public Long getId() {
        return id;
    }

    public int getContractRegisteredYear() {
        return contractRegisteredDate.getYear();
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    @Override
    public String toString() {
        return "Lease [id=" + id + ", discountRate=" + discountRate
                + ", contractStartDate=" + contractStartDate + ", contractEndDate=" + contractEndDate
                + ", totalPayment=" + totalPayment + ", advancePayment=" + advancePayment + ", initialDirectCost="
                + initialDirectCost + ", leaseIncentive=" + leaseIncentive + ", numberOfInstallments="
                + ", authorization=" + authorization + ", installmentDetails="
                + installmentDetails + ", contractRegisteredDate = " + contractRegisteredDate
                + ", contractType = " + contractType + ", branch_id =  " + branch + "]";
    }

}
