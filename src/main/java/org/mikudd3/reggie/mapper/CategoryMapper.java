package org.mikudd3.reggie.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mikudd3.reggie.entity.Category;

/**
 * 菜品Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
