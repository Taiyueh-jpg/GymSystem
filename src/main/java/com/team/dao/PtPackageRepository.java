package com.team.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team.model.PtPackage;

public interface PtPackageRepository extends JpaRepository<PtPackage, Long> {

    List<PtPackage> findByStatus(Integer status);
}