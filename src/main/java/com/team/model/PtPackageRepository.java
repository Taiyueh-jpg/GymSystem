package com.team.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PtPackageRepository extends JpaRepository<PtPackage, Long> {

    List<PtPackage> findByStatus(Integer status);
}