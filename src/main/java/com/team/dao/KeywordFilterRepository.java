package com.team.dao;

import com.team.model.KeywordFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeywordFilterRepository extends JpaRepository<KeywordFilter, Long> {

    // 取得所有啟用中的關鍵字（掃描用）
    List<KeywordFilter> findByStatus(String status);

    // 取得特定 type 且啟用中的關鍵字
    List<KeywordFilter> findByTypeAndStatus(String type, String status);

    // 關鍵字是否已存在（防止重複新增）
    boolean existsByKeywordIgnoreCase(String keyword);

    // Admin 管理用：依 type 篩選
    List<KeywordFilter> findByType(String type);

    // 搜尋關鍵字（模糊）
    @Query("SELECT k FROM KeywordFilter k WHERE LOWER(k.keyword) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<KeywordFilter> searchByKeyword(String keyword);
}
