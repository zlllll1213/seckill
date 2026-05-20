package com.example.seckill.product.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.seckill.common.Result;
import com.example.seckill.product.entity.Product;
import com.example.seckill.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ========== 公开接口 ==========

    /**
     * 分页查询商品列表（公开）
     */
    @GetMapping("/api/products")
    public Result<IPage<Product>> listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        return Result.success(productService.listProducts(Math.max(page, 1), safeSize));
    }

    /**
     * 查询商品详情（公开）
     */
    @GetMapping("/api/products/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        return Result.success(productService.getProduct(id));
    }

    // ========== 管理员接口 ==========

    /**
     * 新增商品（ADMIN）
     */
    @PostMapping("/api/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Product> createProduct(@RequestBody Product product) {
        return Result.success(productService.createProduct(product));
    }

    /**
     * 更新商品（ADMIN）
     */
    @PutMapping("/api/admin/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return Result.success(productService.updateProduct(id, product));
    }
}
