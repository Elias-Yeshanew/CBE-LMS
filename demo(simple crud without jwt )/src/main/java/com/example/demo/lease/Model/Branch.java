package com.example.demo.lease.Model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
@Table(name = "branches")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long id;

    @Column(name = "branch_name")
    private String branchName;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "location")
    private Long location;

    @Column(name = "cost_center")
    private Long costCenter;

    @ManyToOne
    @JoinColumn(name = "district_id", referencedColumnName = "district_id")
    private District district;

    @ManyToOne
    @JoinColumn(name = "political_region_id", referencedColumnName = "political_region_id")
    private PoliticalRegion politicalRegion;

    @OneToMany(mappedBy = "branch", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Lease> leases;

    public Long getId() {
        return id;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public PoliticalRegion getPoliticalRegion() {
        return politicalRegion;
    }

    public void setPoliticalRegion(PoliticalRegion politicalRegion) {
        this.politicalRegion = politicalRegion;
    }

    public List<Lease> getLeases() {
        return leases;
    }

    public void setLeases(List<Lease> leases) {
        this.leases = leases;
    }

    public Long getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(Long costCenter) {
        this.costCenter = costCenter;
    }

    public Long getlocation() {
        return location;
    }

    public void setLocation(Long location) {
        this.location = location;
    }
}
