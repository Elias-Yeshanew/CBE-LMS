package com.cbe.lms.lease.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.cbe.lms.lease.Model.Region;
import com.cbe.lms.lease.Repository.DistrictRepository;
import com.cbe.lms.lease.Repository.RegionRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class RegionService {
    private final RegionRepository regionRepository;
    private final DistrictRepository districtRepository;

    public Region addNewRegion(Region region) throws Exception {
        return regionRepository.save(region);
    }

    public Map<String, Object> getAllRegions(int page, int size) {
        PageRequest pageable = PageRequest.of(page - 1, size);
        Page<Region> regionPage = regionRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();

        response.put("pagination", PaginationUtil.buildPagination(page, size, regionPage.getTotalElements()));

        response.put("regions",
                regionPage.getContent().stream().map(this::mapRegion).collect(Collectors.toList()));

        return response;
    }

    public Map<String, Object> getRegionById(long regionId) {
        Optional<Region> optionalRegion = regionRepository.findById(regionId);

        if (optionalRegion.isPresent()) {

            Map<String, Object> response = new HashMap<>();
            response.put("region", optionalRegion
                    .map(this::mapRegion));
            return response;
        } else {
            return null;
        }
    }

    private Map<String, Object> mapRegion(Region region) {

        Long districtCount = districtRepository.countByRegion(region);
        Map<String, Object> regionData = new HashMap<>();
        regionData.put("regionId", region.getRegionId());
        regionData.put("regionName", region.getRegionName());
        regionData.put("districtCount", districtCount);
        // Include other region fields

        return regionData;
    }

    public Region updateRegionById(Long regionId, Region updatedRegion) throws Exception {
        Region existingRegion = regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("Region not found"));

        // Update fields based on your requirements
        existingRegion.setRegionName(updatedRegion.getRegionName());

        return regionRepository.save(existingRegion);
    }

}
