package com.cbe.lms.lease.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cbe.lms.lease.Service.ReportServiceHierarchy;
import com.cbe.lms.lease.hierarchyRequest;

@RestController
@RequestMapping("/leases")
public class reportControllerHierarchy {

    private final ReportServiceHierarchy reportServiceHierarchy;

    public reportControllerHierarchy(ReportServiceHierarchy reportServiceHierarchy) {
        this.reportServiceHierarchy = reportServiceHierarchy;
    }

    @GetMapping("/hierarchy/reports")
    public List<Map<String, Object>> getHierarchy() {
        return reportServiceHierarchy.getRegionreportHierarchy();
    }

    @GetMapping("/branch/{branchId}")
    public Map<String, Object> getHierarchyForBranch(@PathVariable Long branchId) {
        return reportServiceHierarchy.getHierarchyForBranch(branchId);
    }

    @GetMapping("/hierarchy/branches")
    public String getBranchHierarchies(@RequestBody hierarchyRequest requestBody) {
        return reportServiceHierarchy.getHierarchyForBranches(requestBody.getBranchIds()).toString();
    }

}
