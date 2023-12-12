package com.example.demo.lease.Service;

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
    @CsvBindByName(column = "DistrtictName")
    private String districtName;

}
