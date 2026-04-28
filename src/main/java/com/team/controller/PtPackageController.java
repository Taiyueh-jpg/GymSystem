package com.team.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.team.model.PtPackage;
import com.team.service.PtPackageService;

@RestController
@RequestMapping("/pt-packages")
@CrossOrigin
public class PtPackageController {

    @Autowired
    private PtPackageService ptPackageService;

    @GetMapping
    public List<PtPackage> getAllPackages() {
        return ptPackageService.findAllPackages();
    }

    @GetMapping("/active")
    public List<PtPackage> getActivePackages() {
        return ptPackageService.findActivePackages();
    }
}