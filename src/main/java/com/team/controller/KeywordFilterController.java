package com.team.controller;

import com.team.model.KeywordFilter;
import com.team.service.KeywordFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/keyword-filter")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"}, allowCredentials = "true")
public class KeywordFilterController {

    @Autowired
    private KeywordFilterService keywordFilterService;

    // 取得全部
    @GetMapping
    public ResponseEntity<List<KeywordFilter>> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(keywordFilterService.search(search));
        }
        if (type != null && !type.isBlank()) {
            return ResponseEntity.ok(keywordFilterService.getByType(type));
        }
        return ResponseEntity.ok(keywordFilterService.getAll());
    }

    // 新增
    @PostMapping
    public ResponseEntity<?> add(@RequestBody KeywordFilter kf) {
        try {
            return ResponseEntity.ok(keywordFilterService.add(kf));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 更新
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody KeywordFilter kf) {
        try {
            return ResponseEntity.ok(keywordFilterService.update(id, kf));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 啟用 / 停用切換
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(keywordFilterService.toggleStatus(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 刪除
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        keywordFilterService.delete(id);
        return ResponseEntity.ok(Map.of("message", "已刪除"));
    }
}
