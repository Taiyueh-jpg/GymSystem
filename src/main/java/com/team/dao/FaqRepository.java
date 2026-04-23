package com.team.dao;

import com.team.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {  // Long for BIGINT

    List<Faq> findByStatusOrderByCategoryAscDisplayOrderAsc(String status);

    List<Faq> findByCategoryAndStatusOrderByDisplayOrderAsc(String category, String status);

    @Query("SELECT DISTINCT f.category FROM Faq f WHERE f.status = 'active' ORDER BY f.category")
    List<String> findDistinctCategories();

    @Query("SELECT f FROM Faq f WHERE f.status = 'active' AND " +
           "(f.question LIKE %:keyword% OR f.answer LIKE %:keyword%) " +
           "ORDER BY f.category ASC, f.displayOrder ASC")
    List<Faq> searchByKeyword(String keyword);
}
