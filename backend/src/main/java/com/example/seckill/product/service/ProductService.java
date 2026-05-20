package com.example.seckill.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.seckill.product.entity.Product;

public interface ProductService {

    /**
     * 分页查询商品列表
     *
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @return 分页结果
     */
    IPage<Product> listProducts(int page, int size);

    /**
     * 根据 ID 查询商品详情
     *
     * @param id 商品 ID
     * @return 商品实体
     */
    Product getProduct(Long id);

    /**
     * 新增商品
     *
     * @param product 商品信息
     * @return 保存后的商品实体
     */
    Product createProduct(Product product);

    /**
     * 更新商品
     *
     * @param id      商品 ID
     * @param product 更新内容
     * @return 更新后的商品实体
     */
    Product updateProduct(Long id, Product product);
}
