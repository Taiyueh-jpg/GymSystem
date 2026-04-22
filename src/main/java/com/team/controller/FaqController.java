package com.team.controller;

import com.team.model.Faq;
import com.team.service.FaqService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faq")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class FaqController {

    @Autowired
    private FaqService faqService;

    /**
     * GET /api/faq
     * 取得所有啟用 FAQ（flat list）
     */
    @GetMapping
    public ResponseEntity<List<Faq>> getAllFaqs() {
        return ResponseEntity.ok(faqService.getAllActiveFaqs());
    }

    /**
     * GET /api/faq/grouped
     * 取得依分類分組的 FAQ Map
     */
    @GetMapping("/grouped")
    public ResponseEntity<Map<String, List<Faq>>> getGroupedFaqs() {
        return ResponseEntity.ok(faqService.getAllFaqGroupedByCategory());
    }

    /**
     * GET /api/faq/categories
     * 取得所有分類
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(faqService.getAllCategories());
    }

    /**
     * GET /api/faq/category/{category}
     * 依分類取得 FAQ
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Faq>> getFaqsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(faqService.getFaqsByCategory(category));
    }

    /**
     * GET /api/faq/search?keyword=xxx
     * 關鍵字搜尋
     */
    @GetMapping("/search")
    public ResponseEntity<List<Faq>> searchFaqs(@RequestParam String keyword) {
        return ResponseEntity.ok(faqService.searchFaqs(keyword));
    }

    /**
     * GET /api/faq/{id}
     * 取得單一 FAQ
     */
    @GetMapping("/{id}")
    public ResponseEntity<Faq> getFaqById(@PathVariable Long id) {
        return faqService.getFaqById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Faq> createFaq(@RequestBody Faq faq) {
        return ResponseEntity.ok(faqService.createFaq(faq));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Faq> updateFaq(@PathVariable Long id, @RequestBody Faq faq) {
        try {
            return ResponseEntity.ok(faqService.updateFaq(id, faq));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        faqService.deleteFaq(id);
        return ResponseEntity.noContent().build();
    }
}
