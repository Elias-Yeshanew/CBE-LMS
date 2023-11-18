package com.example.demo.lease.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Service.BranchService;

@RestController
@RequestMapping("/branch")
public class BranchController {
    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    @GetMapping("/{politicalRegion}")
    public ResponseEntity<Branch> getBranchByPoliticalRegion(@PathVariable String politicalRegion) {
        Branch branch = branchService.getBranchByPoliticalRegion(politicalRegion);
        if (branch != null) {
            return new ResponseEntity<>(branch, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
