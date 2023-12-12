package com.example.demo.lease.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody; // Add this import

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

    @PostMapping("/addBranch")
    public ResponseEntity<Branch> addtestBranch(@RequestBody Branch branch) throws Exception {
        try {
            Branch newBranch = branchService.addNewBranch(branch);
            return new ResponseEntity<>(newBranch, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}
