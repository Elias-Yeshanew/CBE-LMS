package com.example.demo.lease.Repository;

import java.time.LocalDate;
import java.util.List;

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

        @Query("SELECT l FROM Lease l WHERE l.branch.district.id = :districtId")
        List<Lease> findByDistrictIdQuery(@Param("districtId") Long districtId);

        List<Lease> findByBranch(Branch branch);

        List<Lease> findAllByAuthorizationTrue();

        Page<Lease> findByContractEndDateBeforeAndAuthorizationIsTrue(LocalDate date, PageRequest pageable);

        Page<Lease> findByContractEndDateBetweenAndAuthorizationIsTrue(int startYear, int endYear,
                        PageRequest pageable);

        Page<Lease> findByContractEndDateAfterAndAuthorizationIsTrue(LocalDate date, PageRequest pageable);

        @Query("SELECT l FROM Lease l " +
                        "WHERE (:startYear IS null OR YEAR(l.contractRegisteredDate) = :startYear) " +
                        "OR (:endYear IS null OR YEAR(l.contractEndDate) = :endYear )")
        Page<Lease> findByContractStartDateYearORContractEndDateYear(int startYear,
                        int endYear, PageRequest pageable);

        @Query("SELECT l FROM Lease l " +
                        "WHERE (:startYear IS null OR YEAR(l.contractRegisteredDate) = :startYear) " +
                        "And (:endYear IS null OR YEAR(l.contractEndDate) = :endYear )")
        Page<Lease> findByContractStartDateYearAndContractEndDateYear(int startYear,
                        int endYear, PageRequest pageable);

        @Query("SELECT l, b.branchName as branchName FROM Lease l JOIN l.branch b WHERE b.id = :branchId")
        List<Lease> findByBranchIdWithBranchName(@Param("branchId") Long branchId);

        List<Lease> findByBranchIdAndContractRegisteredDateContaining(Long branchId, Integer contractRegisteredDate);

        @Query("SELECT l FROM Lease l " +
                        "WHERE YEAR(l.contractRegisteredDate) = :registeredYear " +
                        "AND YEAR(l.contractEndDate) = :endYear " +
                        "AND l.authorization = true " +
                        "AND l.contractEndDate < :currentDate")
        Page<Lease> findExpiredLeasesWithAdditionalFilter(@Param("currentDate") LocalDate currentDate,
                        @Param("registeredYear") int registeredYear,
                        @Param("endYear") int endYear,
                        PageRequest pageable);


        @Query("SELECT l FROM Lease l WHERE l.branch.id = :branchId AND FUNCTION('YEAR', l.contractRegisteredDate) = :year")
        List<Lease> findByBranchIdAndContractRegisteredDateYear(@Param("branchId") Long branchId,
                        @Param("year") int year);

        @Query("SELECT l FROM Lease l WHERE l.branch.district.id = :districtId AND (:contractRegisteredDate IS null OR YEAR(l.contractRegisteredDate) = :contractRegisteredDate)")
        List<Lease> findByBranchDistrictDistrictIdAndContractRegisteredDateYear(
                        @Param("districtId") Long districtId,
                        @Param("contractRegisteredDate") Integer contractRegisteredDate);

        @Query("SELECT l FROM Lease l JOIN l.branch b WHERE b.district.districtId = :districtId AND EXTRACT(YEAR FROM l.contractRegisteredDate) = :year")
        List<Lease> findByDistrictIdAndContractRegisteredDateYear(Long districtId, Integer year);

        // @Query("SELECT l FROM Lease l WHERE l.districtId = :districtId AND
        // FUNCTION('YEAR', l.contractRegisteredDate) >= :startYear")
        // List<Lease>
        // findByDistrictIdAndContractRegisteredDateYear(@Param("districtId") Long
        // districtId,
        // @Param("startYear") int startYear);

        // @Query("SELECT l FROM Lease l WHERE l.branch.district.id = :districtId")
        // List<Lease>
        // findByDistrictIdAndContractRegisteredDateYear(@Param("districtId") Long
        // districtId);

        @Query("SELECT l FROM Lease l WHERE l.branch.district.id = :districtId AND YEAR(l.contractRegisteredDate) = :startYear")
        List<Lease> findByDistrictIdAndContractRegisteredDateYear(
                        @Param("districtId") Long districtId,
                        @Param("startYear") int startYear);

}
