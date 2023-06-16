package org.mikudd3.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mikudd3.reggie.entity.Dish;
import org.mikudd3.reggie.entity.SetmealDish;
import org.mikudd3.reggie.mapper.DishMapper;
import org.mikudd3.reggie.mapper.SetmealDishMapper;
import org.mikudd3.reggie.service.DishService;
import org.mikudd3.reggie.service.SetmealDishService;
import org.springframework.stereotype.Service;

/**
 * @project:
 * @author: mikudd3
 * @version: 1.0
 */
@Service
public class SetmealDishServiceImpl extends ServiceImpl<SetmealDishMapper, SetmealDish> implements SetmealDishService {
}
