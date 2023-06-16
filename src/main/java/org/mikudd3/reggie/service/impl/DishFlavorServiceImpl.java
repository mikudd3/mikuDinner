package org.mikudd3.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mikudd3.reggie.entity.DishFlavor;
import org.mikudd3.reggie.mapper.DishFlavorMapper;
import org.mikudd3.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

/**
 * @project: 菜品口味service实现
 * @author: mikudd3
 * @version: 1.0
 */

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
