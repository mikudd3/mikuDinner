package org.mikudd3.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mikudd3.reggie.dto.DishDto;
import org.mikudd3.reggie.dto.SetmealDto;
import org.mikudd3.reggie.entity.Setmeal;

import java.util.List;

/**
 * 套餐service接口
 */
public interface SetmealService extends IService<Setmeal> {

    //新增套餐
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐
    public void removeWithDish(List<Long> ids);

    //根据id获取信息
    public SetmealDto getByIdWithDish(Long id);

    //更新套餐信息
    public void updateWithDish(SetmealDto setmealDto);


}
