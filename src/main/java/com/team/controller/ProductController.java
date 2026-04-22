package com.team.controller;

import com.team.model.Product;
import com.team.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ==========================================
// 🛒 同學 C 負責區域：商品控制器
// ==========================================

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    // ============================================================
    // 【前台】頁面路由 - 回傳 HTML 頁面給瀏覽器
    // ============================================================

    /**
     * 前台商城頁面
     * 網址：GET /product/mall
     */
    @GetMapping("/product/mall")
    public String mallPage(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product/mall"; // 對應 templates/product/mall.html
    }

    // ============================================================
    // 【後台】頁面路由
    // ============================================================

    /**
     * 後台商品管理頁面
     * 網址：GET /admin/product
     */
    @GetMapping("/admin/product")
    public String adminProductPage(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product/product-admin"; // 對應 templates/product/product-admin.html
    }

    // ============================================================
    // 【REST API】給前端 JS (fetch) 呼叫
    // ============================================================

    /**
     * 查詢全部商品
     * GET /api/products
     */
    @GetMapping("/api/products")
    @ResponseBody
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    /**
     * 關鍵字搜尋商品
     * GET /api/products/search?keyword=乳清
     */
    @GetMapping("/api/products/search")
    @ResponseBody
    public List<Product> searchProducts(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    /**
     * 查詢單一商品（供同學 D 購物車使用）
     * GET /api/products/{id}
     */
    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getProductById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * 新增商品（後台用）
     * POST /api/products
     */
    @PostMapping("/api/products")
    @ResponseBody
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            return ResponseEntity.ok(productService.createProduct(product));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 修改商品（後台用）
     * PUT /api/products/{id}
     */
    @PutMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            return ResponseEntity.ok(productService.updateProduct(id, product));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 刪除商品（後台用）
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok("商品刪除成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
