package com.cbe.lms.lease.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cbe.lms.lease.Service.HirerachyService;

@RestController
@RequestMapping("/leases")
public class RegionDistrictBranchController {

    private final HirerachyService hirerachyService;

    public RegionDistrictBranchController(HirerachyService hirerachyService) {
        this.hirerachyService = hirerachyService;
    }

    @GetMapping("/hierarchy")
    public List<Map<String, Object>> getHierarchy() {
        return hirerachyService.getRegionHierarchy();
    }
}
