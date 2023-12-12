package com.example.demo.lease.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.lease.Model.Region;
import com.example.demo.lease.Service.RegionService;

import org.springframework.web.bind.annotation.RequestBody;
import lombok.AllArgsConstructor;

@CrossOrigin
@AllArgsConstructor
@RestController
@RequestMapping("/region")
public class RegionController {

    private final RegionService regionService;

    @PostMapping
    public ResponseEntity<Region> addNewRegion(@RequestBody Region region) throws Exception {
        try {
            Region newRegion = regionService.addNewRegion(region);
            return new ResponseEntity<>(newRegion, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
