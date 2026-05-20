package com.example.seckill.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.seckill.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
