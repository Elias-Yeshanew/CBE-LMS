package com.example.demo.lease.Controller;

import com.example.demo.lease.Model.District;
import com.example.demo.lease.Service.DistrictService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/districts")
public class DistrictController {

    private final DistrictService districtService;

    public DistrictController(DistrictService districtService) {
        this.districtService = districtService;
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getDistrictCount() {
        long count = districtService.getDistrictCount();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @PostMapping("/addDistrict")
    public ResponseEntity<District> addDistrict(@RequestBody District district) throws Exception {
        try {
            District newDistrict = districtService.addNewDistrict(district);
            return new ResponseEntity<>(newDistrict, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

}
