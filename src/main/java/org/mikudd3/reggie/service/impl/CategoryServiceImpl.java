package org.mikudd3.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mikudd3.reggie.common.CustomException;
import org.mikudd3.reggie.entity.Category;
import org.mikudd3.reggie.entity.Dish;
import org.mikudd3.reggie.entity.Setmeal;
import org.mikudd3.reggie.mapper.CategoryMapper;
import org.mikudd3.reggie.service.CategoryService;
import org.mikudd3.reggie.service.DishService;
import org.mikudd3.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * @project:
 * @author: mikudd3
 * @version: 1.0
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id进行菜品删除
     *
     * @param id
     */
    @Override
    public void remove(Long id) {

        //1. 判断当前菜品是否关联了其他菜品
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //设置条件
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        //执行查询
        int count1 = dishService.count(dishLambdaQueryWrapper);
        if (count1 > 0) {
            //已经关联了菜品，抛出异常
            throw new CustomException("当前菜品关联了其他菜品，不能直接删除");
        }

        //2.判断当前才是否关联了其他菜
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //设置条件
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        //执行查询
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //判断
        if (count2 > 0) {
            //已经关联了套餐，抛出异常
            throw new CustomException("当前菜品关联了其他套餐，不能直接删除");
        }

        //3.如果没有关联上述条件，则直接执行删除
        super.removeById(id);
    }
}
