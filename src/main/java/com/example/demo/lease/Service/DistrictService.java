package com.example.demo.lease.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.lease.Model.District;
import com.example.demo.lease.Model.Region;
import com.example.demo.lease.Repository.BranchRepository;
import com.example.demo.lease.Repository.DistrictRepository;
import com.example.demo.lease.Repository.RegionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistrictService {

    private final DistrictRepository districtRepository;
    private final RegionRepository regionRepository;
    private final BranchRepository branchRepository;

    public long getDistrictCount() {
        return districtRepository.count();
    }

    public List<District> saveAllDistricts(List<District> districts) {
        return districtRepository.saveAll(districts);
    }

    public District addNewDistrict(District district) throws Exception {
        // Load the Region from the database using its regionId
        Region existingRegion = regionRepository.findById(district.getRegion().getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        // Set the loaded Region in the District entity
        district.setRegion(existingRegion);

        // Save the District entity
        return districtRepository.save(district);
    }

    public Map<String, Object> getAllDistricts(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<District> districtPage = districtRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, districtPage.getTotalElements()));

        response.put("districts",
                districtPage.getContent().stream().map(this::mapDistrict).collect(Collectors.toList()));

        return response;
    }

    private Map<String, Object> mapDistrict(District district) {

        Long branchCount = branchRepository.countByDistrict(district);
        Map<String, Object> districtData = new HashMap<>();
        districtData.put("districtId", district.getDistrictId());
        districtData.put("districtName", district.getDistrictName());
        districtData.put("region", district.getRegion());
        districtData.put("leaseLiabilityAccount", district.getLeaseLiabilityAccount());
        districtData.put("rouAccount", district.getRouAccount());
        districtData.put("branchCount", branchCount);
        // Include other district fields

        return districtData;
    }

    public District updateDistrictById(Long districtId, District updatedDistrict) throws Exception {
        District existingDistrict = districtRepository.findById(districtId)
                .orElseThrow(() -> new IllegalArgumentException("District not found"));

        // Update fields based on your requirements
        existingDistrict.setDistrictName(updatedDistrict.getDistrictName());
        existingDistrict.setLeaseLiabilityAccount(updatedDistrict.getLeaseLiabilityAccount());
        existingDistrict.setRouAccount(updatedDistrict.getRouAccount());

        // Update the region if it's provided
        if (updatedDistrict.getRegion() != null) {
            Region existingRegion = regionRepository.findById(updatedDistrict.getRegion().getRegionId())
                    .orElseThrow(() -> new IllegalArgumentException("Region not found"));
            existingDistrict.setRegion(existingRegion);
        }

        return districtRepository.save(existingDistrict);
    }

    public Map<String, Object> getDistrictById(Long districtId) throws Exception {
        Optional<District> optionalDistrict = districtRepository.findById(districtId);
        Map<String, Object> response = new HashMap<>();

        if (optionalDistrict.isPresent()) {

            response.put("district",
                    optionalDistrict.map(this::mapDistrict));
            return response;
        } else {
            return null;
        }

    }

    public void deleteDistrictById(Long districtId) {
        districtRepository.deleteById(districtId);
    }
}
