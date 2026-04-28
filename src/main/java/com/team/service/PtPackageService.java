package com.team.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.team.model.PtPackage;
import com.team.model.PtPackageRepository;

@Service
public class PtPackageService {

    @Autowired
    private PtPackageRepository ptPackageRepository;

    public List<PtPackage> findAllPackages() {
        return ptPackageRepository.findAll();
    }

    public List<PtPackage> findActivePackages() {
        return ptPackageRepository.findByStatus(1);
    }

    public PtPackage findPackageById(Long packageId) {
        Optional<PtPackage> packageOpt = ptPackageRepository.findById(packageId);

        if (packageOpt.isEmpty()) {
            return null;
        }

        return packageOpt.get();
    }
}