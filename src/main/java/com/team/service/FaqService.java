package com.team.service;

import com.team.dao.FaqRepository;
import com.team.model.Faq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FaqService {

    @Autowired
    private FaqRepository faqRepository;

    /**
     * 取得所有啟用的 FAQ，回傳以分類分組的 Map
     * key = 分類名稱, value = 該分類的 FAQ 清單
     */
    public Map<String, List<Faq>> getAllFaqGroupedByCategory() {
        List<Faq> faqs = faqRepository.findByStatusOrderByCategoryAscDisplayOrderAsc("active");
        Map<String, List<Faq>> grouped = new LinkedHashMap<>();
        for (Faq faq : faqs) {
            String cat = faq.getCategory() != null ? faq.getCategory() : "其他";
            grouped.computeIfAbsent(cat, k -> new ArrayList<>()).add(faq);
        }
        return grouped;
    }

    public List<Faq> getAllActiveFaqs() {
        return faqRepository.findByStatusOrderByCategoryAscDisplayOrderAsc("active");
    }

    /**
     * 取得所有分類名稱
     */
    public List<String> getAllCategories() {
        return faqRepository.findDistinctCategories();
    }

    /**
     * 依分類取得 FAQ
     */
    public List<Faq> getFaqsByCategory(String category) {
        return faqRepository.findByCategoryAndStatusOrderByDisplayOrderAsc(category, "active");
    }

    public Optional<Faq> getFaqById(Long id) {
        return faqRepository.findById(id);
    }

    /**
     * 關鍵字搜尋
     */
    public List<Faq> searchFaqs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveFaqs();
        }
        return faqRepository.searchByKeyword(keyword.trim());
    }

    /**
     * 新增 FAQ
     */
    public Faq createFaq(Faq faq) {
        return faqRepository.save(faq);
    }

    /**
     * 更新 FAQ
     */
    public Faq updateFaq(Long id, Faq updated) {
        return faqRepository.findById(id).map(faq -> {
            faq.setQuestion(updated.getQuestion());
            faq.setAnswer(updated.getAnswer());
            faq.setCategory(updated.getCategory());
            faq.setDisplayOrder(updated.getDisplayOrder());
            faq.setStatus(updated.getStatus());
            return faqRepository.save(faq);
        }).orElseThrow(() -> new RuntimeException("FAQ not found: " + id));
    }

    public void deleteFaq(Long id) {
        faqRepository.deleteById(id);
    }
}
