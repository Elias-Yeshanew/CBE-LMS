package com.cbe.lms.lease.Model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "districts")
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Long districtId;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "leaseLiablity_account")
    private String leaseLiabilityAccount;

    @Column(name = "rou_account")
    private String rouAccount;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "region_id", referencedColumnName = "region_id")
    private Region region;

    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL)
    @JsonIgnore // Ignore this property during serialization
    private List<Branch> branches;

    public Long getDistrictId() {
        return districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getLeaseLiabilityAccount() {
        return leaseLiabilityAccount;
    }

    public void setLeaseLiabilityAccount(String leaseLiabilityAccount) {
        this.leaseLiabilityAccount = leaseLiabilityAccount;
    }

    public String getRouAccount() {
        return rouAccount;
    }

    public void setRouAccount(String rouAccount) {
        this.rouAccount = rouAccount;
    }
}
