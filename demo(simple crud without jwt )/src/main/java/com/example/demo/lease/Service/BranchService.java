package com.example.demo.lease.Service;

import org.springframework.stereotype.Service;

import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Repository.BranchRepository;

@Service
public class BranchService {

    private final BranchRepository branchRepository;

    public BranchService(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    public Branch getBranchByPoliticalRegion(String politicalRegion) {
        return branchRepository.findByPoliticalRegion(politicalRegion);
    }
}
