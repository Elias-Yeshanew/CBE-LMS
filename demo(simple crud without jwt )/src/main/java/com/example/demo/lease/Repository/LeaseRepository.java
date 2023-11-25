package com.example.demo.lease.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springdoc.core.converters.models.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.demo.lease.Model.Branch;
import com.example.demo.lease.Model.Lease;

@Repository
public interface LeaseRepository extends JpaRepository<Lease, Long> {
        Lease findById(long id);

        List<Lease> findByBranchId(Long branchId);

        List<Lease> findAllByAuthorizationFalse();

        long countByAuthorization(boolean authorization);

        long countByContractEndDateAfterAndAuthorization(LocalDate currentDate, boolean authorization);

        long countByContractEndDateBeforeAndAuthorization(LocalDate currentDate, boolean authorization);

        List<Lease> findByBranchIdIn(List<Long> branchIds);

        List<Lease> getLeasesByBranchId(Long branchId);

        List<Lease> findByBranch(Branch branch);

        List<Lease> findAllByAuthorizationTrue();

        List<Lease> findByContractEndDateBeforeAndAuthorizationIsTrue(LocalDate currentDate);

        List<Lease> findByContractEndDateAfterAndAuthorizationIsTrue(LocalDate currentDate);

        @Query("SELECT l FROM Lease l " +
                        "WHERE YEAR(l.contractRegisteredDate) = :startYear " +
                        "OR (:endYear IS NULL OR YEAR(l.contractEndDate) = :endYear)")
        Page<Lease> findByContractStartDateYearAndContractEndDateYear(int startYear, int endYear, PageRequest pageable);

        @Query("SELECT l FROM Lease l " +
                        "WHERE (:startYear IS NULL OR YEAR(l.contractRegisteredDate) = :startYear) " +
                        "AND (:endYear IS NULL OR YEAR(l.contractEndDate) = :endYear)")
        Page<Lease> findByContractStartYearOrEndYear(@Param("startYear") Integer startYear,
                        @Param("endYear") Integer endYear, PageRequest pageable);

        @Query("SELECT l FROM Lease l " +
                        "WHERE (:startYear IS NULL OR YEAR(l.contractRegisteredDate) = :startYear)")
        Page<Lease> findByContractStartYear(@Param("startYear") Integer startYear, PageRequest pageable);

        // Page<Lease> findByContractYearRange(int startYear, int endYear, PageRequest
        // pageable);

}
