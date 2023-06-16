package org.mikudd3.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mikudd3.reggie.common.CustomException;
import org.mikudd3.reggie.dto.DishDto;
import org.mikudd3.reggie.entity.Dish;
import org.mikudd3.reggie.entity.DishFlavor;
import org.mikudd3.reggie.mapper.DishMapper;
import org.mikudd3.reggie.service.DishFlavorService;
import org.mikudd3.reggie.service.DishService;
import org.mikudd3.reggie.service.SetmealDishService;
import org.mikudd3.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @project: 菜品service
 * @author: mikudd3
 * @version: 1.0
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * //新增菜品，同时加入到菜品表合口味表，需要操作两张表，dish和dishFlavor
     *
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保持菜品的基本信息到菜品表
        this.save(dishDto);

        //保持菜品口味信息到口味表
        Long id = dishDto.getId();//菜品id

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        //将返回结果进行处理
        flavors = flavors.stream().map((item) -> {
            item.setDishId(id);
            return item;
        }).collect(Collectors.toList());//将返回结果重新变成一个list集合


        dishFlavorService.saveBatch(dishDto.getFlavors());
    }


    /**
     * 根据id返回菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //根据id获取Dish对象
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        //对象拷贝，拷贝普通属性
        BeanUtils.copyProperties(dish, dishDto);

        //根据菜品的id返回口味对象
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        //设置等值条件
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        //进行查询
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        //对DishDto的口味进行赋值
        dishDto.setFlavors(list);


        return dishDto;
    }


    /**
     * 更新菜品信息
     *
     * @param dishDto
     */
    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表数据
        this.updateById(dishDto);

        //先清理当前菜品的口味信息
        //根据dishId清理口味表数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        //设置等值条件
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //将新的菜品口味信息添加到口味表中
        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        //将返回结果进行处理
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());//将返回结果重新变成一个list集合
        dishFlavorService.saveBatch(dishDto.getFlavors());
    }

    /**
     * 套餐批量删除和单个删除
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        //根据菜品id查询菜品
        //根据id查询dish对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //设置等值查询
        queryWrapper.in(ids != null, Dish::getId, ids);
        //进行查询
        List<Dish> list = this.list(queryWrapper);

        //遍历集合
        for (Dish dish : list) {
            //获取当前菜品状态
            Integer status = dish.getStatus();

            if (status != 0) {
                //当前菜品正在售卖，无法删除
                throw new CustomException("当前菜品正在售卖，无法删除");
            } else {
                //执行删除
                this.removeById(dish.getId());
            }
        }

    }


}
