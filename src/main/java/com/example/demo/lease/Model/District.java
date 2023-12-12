package com.example.demo.lease.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

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

}
