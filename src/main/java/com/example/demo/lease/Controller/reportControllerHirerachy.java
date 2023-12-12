package com.example.demo.lease.Controller;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.lease.hirarcyRequset;
import com.example.demo.lease.Service.ReportServiceHierarchy;

@RestController
@RequestMapping("/leases")
public class reportControllerHirerachy {
    private final ReportServiceHierarchy reportServiceHierarchy;

    public reportControllerHirerachy(ReportServiceHierarchy reportServiceHierarchy) {
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
    public String getBranchHierarchies(@RequestBody hirarcyRequset requestBody) {
        return reportServiceHierarchy.getHierarchyForBranches(requestBody.getBranchIds()).toString();
    }

}
