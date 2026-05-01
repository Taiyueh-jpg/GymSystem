package com.team.service;

import com.team.dao.ProductDao;
import com.team.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// ==========================================
// 🛒 同學 C 負責區域：商品業務邏輯
// ==========================================

@Service
public class ProductService {

    @Autowired
    private ProductDao productDao;

    // ── 查詢全部商品（前台商城用）──
    public List<Product> getAllProducts() {
        return productDao.findAll();
    }

    // ── 依 ID 查詢單一商品 ──
    public Product getProductById(Long id) {
        return productDao.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到商品 ID：" + id));
    }

    // ── 關鍵字搜尋商品名稱 ──
    public List<Product> searchProducts(String keyword) {
        return productDao.findByPnameContaining(keyword);
    }

    // ── 新增商品（後台用）──
    public Product createProduct(Product product) {
        if (product.getPname() == null || product.getPname().isBlank()) {
            throw new RuntimeException("商品名稱不能為空！");
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new RuntimeException("價格不能為負數！");
        }
        return productDao.save(product);
    }

    // ── 修改商品（後台用）──
    public Product updateProduct(Long id, Product updatedProduct) {
        // 1. 從資料庫抓出目前的舊資料
        Product existing = getProductById(id);
        
        // 2. 將前端傳來的新資料，一一覆蓋上去
        existing.setPname(updatedProduct.getPname());
        existing.setPrice(updatedProduct.getPrice());
        
        // 🔥 關鍵修復：把前端傳來的庫存數量更新進去！
        if (updatedProduct.getStock() != null) {
            existing.setStock(updatedProduct.getStock());
        }

        // 3. 圖片處理 (有傳新圖片才更新，沒傳則保留舊圖)
        if (updatedProduct.getImageBase64() != null && !updatedProduct.getImageBase64().isBlank()) {
            existing.setImageBase64(updatedProduct.getImageBase64());
        }
        
        // 4. 存回資料庫
        return productDao.save(existing);
    }

    // ── 刪除商品（後台用）──
    public void deleteProduct(Long id) {
        if (!productDao.existsById(id)) {
            throw new RuntimeException("找不到商品 ID：" + id);
        }
        productDao.deleteById(id);
    }
}
