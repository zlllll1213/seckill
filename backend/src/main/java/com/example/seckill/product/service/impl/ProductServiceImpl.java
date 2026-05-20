package com.example.seckill.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.seckill.common.BusinessException;
import com.example.seckill.common.ErrorCode;
import com.example.seckill.common.HtmlSanitizer;
import com.example.seckill.product.entity.Product;
import com.example.seckill.product.mapper.ProductMapper;
import com.example.seckill.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public IPage<Product> listProducts(int page, int size) {
        Page<Product> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .orderByDesc(Product::getCreatedAt);
        return productMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Product getProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    @Override
    public Product createProduct(Product product) {
        sanitize(product);
        product.setId(null);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.insert(product);
        return product;
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        sanitize(product);
        product.setId(id);
        product.setCreatedAt(existing.getCreatedAt());
        product.setUpdatedAt(LocalDateTime.now());
        productMapper.updateById(product);
        return product;
    }

    private void sanitize(Product product) {
        if (!StringUtils.hasText(product.getName())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        if (product.getStock() == null || product.getStock() < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        product.setName(HtmlSanitizer.sanitize(product.getName()));
        product.setDescription(HtmlSanitizer.sanitize(product.getDescription()));
    }
}
