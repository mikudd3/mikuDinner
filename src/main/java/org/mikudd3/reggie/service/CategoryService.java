package org.mikudd3.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.mikudd3.reggie.entity.Category;

/**
 * 菜品service
 */
public interface CategoryService extends IService<Category> {

    void remove(Long id);
}
