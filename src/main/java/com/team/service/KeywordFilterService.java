package com.team.service;

import com.team.dao.KeywordFilterRepository;
import com.team.model.KeywordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KeywordFilterService {

    @Autowired
    private KeywordFilterRepository keywordFilterRepository;

    // ─────────────────────────────────────────────
    // 掃描結果封裝
    // ─────────────────────────────────────────────
    public static class ScanResult {
        public final boolean blocked;   // true → 應直接擋回
        public final boolean flagged;   // true → 應標記 is_flagged
        public final String hitKeyword; // 命中的關鍵字（debug / log 用）

        public ScanResult(boolean blocked, boolean flagged, String hitKeyword) {
            this.blocked = blocked;
            this.flagged = flagged;
            this.hitKeyword = hitKeyword;
        }
    }

    /**
     * 掃描文字內容是否含有關鍵字
     * 優先級：block > flag
     * 只要 subject 或 body 任一命中即觸發
     */
    public ScanResult scan(String subject, String body) {
        List<KeywordFilter> activeKeywords = keywordFilterRepository.findByStatus("active");

        String combined = ((subject != null ? subject : "") + " " + (body != null ? body : "")).toLowerCase();

        String blockHit = null;
        String flagHit = null;

        for (KeywordFilter kf : activeKeywords) {
            if (combined.contains(kf.getKeyword().toLowerCase())) {
                if ("block".equals(kf.getType())) {
                    blockHit = kf.getKeyword();
                    break; // block 優先，找到就停
                } else if ("flag".equals(kf.getType()) && flagHit == null) {
                    flagHit = kf.getKeyword();
                }
            }
        }

        if (blockHit != null) {
            return new ScanResult(true, false, blockHit);
        }
        if (flagHit != null) {
            return new ScanResult(false, true, flagHit);
        }
        return new ScanResult(false, false, null);
    }

    // ─────────────────────────────────────────────
    // Admin CRUD
    // ─────────────────────────────────────────────

    public List<KeywordFilter> getAll() {
        return keywordFilterRepository.findAll();
    }

    public List<KeywordFilter> getByType(String type) {
        return keywordFilterRepository.findByType(type);
    }

    public List<KeywordFilter> search(String keyword) {
        return keywordFilterRepository.searchByKeyword(keyword);
    }

    public KeywordFilter add(KeywordFilter kf) {
        if (keywordFilterRepository.existsByKeywordIgnoreCase(kf.getKeyword())) {
            throw new IllegalArgumentException("關鍵字「" + kf.getKeyword() + "」已存在");
        }
        return keywordFilterRepository.save(kf);
    }

    public KeywordFilter update(Long id, KeywordFilter updated) {
        KeywordFilter existing = keywordFilterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到關鍵字 id=" + id));
        existing.setKeyword(updated.getKeyword());
        existing.setType(updated.getType());
        existing.setStatus(updated.getStatus());
        return keywordFilterRepository.save(existing);
    }

    public void delete(Long id) {
        keywordFilterRepository.deleteById(id);
    }

    public KeywordFilter toggleStatus(Long id) {
        KeywordFilter kf = keywordFilterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到關鍵字 id=" + id));
        kf.setStatus("active".equals(kf.getStatus()) ? "inactive" : "active");
        return keywordFilterRepository.save(kf);
    }
}
