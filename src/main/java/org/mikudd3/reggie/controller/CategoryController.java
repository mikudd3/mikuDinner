package org.mikudd3.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.mikudd3.reggie.common.BaseContext;
import org.mikudd3.reggie.common.R;
import org.mikudd3.reggie.entity.Category;
import org.mikudd3.reggie.entity.Employee;
import org.mikudd3.reggie.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @project:
 * @author: mikudd3
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService service;


    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        //判断数据是否已经传入
        log.info("输入数据: page={}, pageSize = {}", page, pageSize);

        //构造分页构造器
        Page pageInfo = new Page<>(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);

        //执行查询
        service.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 增加菜品分类或套餐
     *
     * @param category
     * @return
     */
    @RequestMapping
    public R<String> save(@RequestBody Category category) {
        //保存菜品
        service.save(category);
        return R.success("添加成功");
    }

    /**
     * 更新菜品分类或套餐分类
     *
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        //更新菜单
        service.updateById(category);
        return R.success("修改成功");
    }

    /**
     * 根据id删除菜品
     *
     * @param id
     * @return
     */
    @DeleteMapping
    public R<String> deleteById(Long id) {
        service.remove(id);
//        service.removeById(id);
        return R.success("删除成功");
    }

    /**
     * 根据条件查询分类数据
     *
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //执行查询
        List<Category> list = service.list(queryWrapper);
        return R.success(list);
    }
}
