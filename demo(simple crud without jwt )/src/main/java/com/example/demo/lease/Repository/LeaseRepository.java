package com.example.demo.lease.Repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Model.Lease;

@Repository
public interface LeaseRepository extends JpaRepository<Lease, Long> {
    Lease findById(long id);

    Lease findByContractNumber(String contractNumber);

    boolean existsByContractNumber(String contractNumber);

    List<Lease> findByBranchId(Long branchId);

    List<Lease> findAllByAuthorizationFalse();

    long countByAuthorization(boolean authorization);

    long countByContractEndDateAfterAndAuthorization(LocalDate currentDate, boolean authorization);

    long countByContractEndDateBeforeAndAuthorization(LocalDate currentDate, boolean authorization);

    List<Lease> findByBranchIdIn(List<Long> branchIds);

    List<Lease> getLeasesByBranchId(Long branchId);

    List<Lease> findByBranch(Branch branch);

    List<Lease> findAllByAuthorizationTrue();

}
