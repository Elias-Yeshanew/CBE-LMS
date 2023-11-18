package com.example.demo.lease.Model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "regions")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long id;

    @Column(name = "region_name")
    private String regionName;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    private List<District> districts;

    public Long getId() {
        return id;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public List<District> getDistricts() {
        return districts;
    }

    public void setDistricts(List<District> districts) {
        this.districts = districts;
    }
}
