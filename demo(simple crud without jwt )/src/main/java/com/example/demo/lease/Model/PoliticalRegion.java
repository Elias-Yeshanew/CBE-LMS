package com.example.demo.lease.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "political_regions")
public class PoliticalRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "political_region_id")
    private Long id;

    @Column(name = "region_name")
    private String regionName;

    // Other fields and methods as needed

    // Getter and Setter for other fields

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }
}
