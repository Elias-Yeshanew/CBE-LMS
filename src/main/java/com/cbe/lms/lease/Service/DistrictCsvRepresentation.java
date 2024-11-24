package com.cbe.lms.lease.Service;

import com.opencsv.bean.CsvBindByName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DistrictCsvRepresentation {
    @CsvBindByName(column = "region_id")
    private long id;
    @CsvBindByName(column = "DistrictName")
    private String districtName;

}
