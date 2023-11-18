package com.example.demo.lease.Model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "districts")
public class District {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "district_id")
    private Long id;

    @Column(name = "district_name")
    private String districtName;

    @ManyToOne
    @JoinColumn(name = "region_id", referencedColumnName = "region_id")
    private Region region;

    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL)
    private List<Branch> branches;

    public Long getId() {
        return id;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }
}
