package com.cbe.lms.lease.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cbe.lms.lease.Service.HierarchyService;

@RestController
@RequestMapping("/leases")
public class RegionDistrictBranchController {

    private final HierarchyService hierarchyService;

    public RegionDistrictBranchController(HierarchyService hierarchyService) {
        this.hierarchyService = hierarchyService;
    }

    @GetMapping("/hierarchy")
    public List<Map<String, Object>> getHierarchy() {
        return hierarchyService.getRegionHierarchy();
    }
}
