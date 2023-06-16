package org.mikudd3.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mikudd3.reggie.dto.DishDto;
import org.mikudd3.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，同时加入到菜品表合口味表，需要操作两张表，dish和dishFlavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id返回菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息
    public void updateWithFlavor(DishDto dishDto);

    //批量删除
    public void deleteByIds(List<Long> ids);


}
