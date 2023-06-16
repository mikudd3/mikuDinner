package org.mikudd3.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mikudd3.reggie.common.CustomException;
import org.mikudd3.reggie.dto.SetmealDto;
import org.mikudd3.reggie.entity.Dish;
import org.mikudd3.reggie.entity.DishFlavor;
import org.mikudd3.reggie.entity.Setmeal;
import org.mikudd3.reggie.entity.SetmealDish;
import org.mikudd3.reggie.mapper.SetmealMapper;
import org.mikudd3.reggie.service.SetmealDishService;
import org.mikudd3.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @project: 套餐service
 * @author: mikudd3
 * @version: 1.0
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {


    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     */
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保持套餐的基本信息到菜品表
        this.save(setmealDto);
        //套餐菜品
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        //将返回结果进行处理
        dishes = dishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());//将返回结果重新变成一个list集合


        setmealDishService.saveBatch(dishes);
    }


    /**
     * 删除套餐
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        //根据套餐id查询菜品
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //设置等值查询
        queryWrapper.in(ids != null, Setmeal::getId, ids);
        //查询套餐状态
        queryWrapper.eq(Setmeal::getStatus, 1);

        int count = this.count(queryWrapper);
        if (count > 0) {
            //当前菜品正在售卖，无法删除
            throw new CustomException("当前套餐正在售卖，无法删除");
        }
        //先删除套餐表中数据
        this.removeByIds(ids);

        //删除其他表中数据
        LambdaQueryWrapper<SetmealDish> wrapper = new LambdaQueryWrapper<>();

        //构造条件
        wrapper.in(SetmealDish::getSetmealId, ids);
        //删除
        setmealDishService.remove(wrapper);
    }


    /**
     * 根据id获取信息
     *
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //根据id获取setmeal对象
        Setmeal setmeal = this.getById(id);

        //创建SetmealDto对象
        SetmealDto setmealDto = new SetmealDto();
        //对象拷贝
        BeanUtils.copyProperties(setmeal, setmealDto);
        //根据套餐id返回菜品对象
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //设置等值条件
        queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        //进行查询
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }


    /**
     * 修改套餐信息
     *
     * @param setmealDto
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        //更新setmeal表数据
        this.updateById(setmealDto);

        //先清理当前菜品信息
        //根据套餐id清理菜品
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        //设置等值条件
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //将新的菜品信息添加到菜品表中
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //将结果进行处理
        setmealDishes = setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDto.getSetmealDishes());

    }


}
