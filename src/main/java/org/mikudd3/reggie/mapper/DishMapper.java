package org.mikudd3.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mikudd3.reggie.entity.Dish;

/**
 * @project: 菜品mapper
 * @author: mikudd3
 * @version: 1.0
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
