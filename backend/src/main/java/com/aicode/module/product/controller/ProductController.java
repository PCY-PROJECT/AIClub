package com.aicode.module.product.controller;

import com.aicode.common.result.R;
import com.aicode.module.product.entity.Product;
import com.aicode.module.product.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductMapper productMapper;

    /** 健康检查 */
    @GetMapping("/ping")
    public R<String> ping() {
        return R.ok("AI 邀请码后端已启动 🚀");
    }

    /** 产品列表（支持分组和分类筛选） */
    @GetMapping
    public R<List<Product>> list(
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer group) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
            .eq(Product::getStatus, 1)
            .eq(group != null && group != 0, Product::getProductGroup, group)
            .eq(category != null && category != 0, Product::getCategory, category)
            .orderByDesc(Product::getSort);
        return R.ok(productMapper.selectList(wrapper));
    }

    /** 产品详情 */
    @GetMapping("/{id}")
    public R<Product> detail(@PathVariable Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) return R.fail(404, "产品不存在");
        return R.ok(product);
    }
}
