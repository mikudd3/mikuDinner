package org.mikudd3.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.mikudd3.reggie.common.BaseContext;
import org.mikudd3.reggie.common.R;
import org.mikudd3.reggie.dto.DishDto;
import org.mikudd3.reggie.dto.SetmealDto;
import org.mikudd3.reggie.entity.Category;
import org.mikudd3.reggie.entity.Dish;
import org.mikudd3.reggie.entity.Setmeal;
import org.mikudd3.reggie.service.CategoryService;
import org.mikudd3.reggie.service.SetmealDishService;
import org.mikudd3.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @project: 套餐controller
 * @author: mikudd3
 * @version: 1.0
 */
@RestController
@RequestMapping("setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache", allEntries = true) //清除setmealCache名称下,所有的缓存数据
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        setmealService.saveWithDish(setmealDto);
        return R.success("添加成功");
    }

    /**
     * 套餐分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，如果传入名字不为空
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo, queryWrapper);
        //对象拷贝,忽略records属性
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");
        //对records属性进行处理
        List<Setmeal> records = pageInfo.getRecords();
        //获取list
        List<SetmealDto> list = records.stream().map((item) -> {
            SetmealDto setmealDto = new SetmealDto();
            //对属性进行拷贝
            BeanUtils.copyProperties(item, setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //获取分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //获取分类的名字
                String categoryName = category.getName();
                //将分类的名字放入到setmealDto中
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }


    /**
     * 删除套餐
     *
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache", allEntries = true) //清除setmealCache名称下,所有的缓存数据
    public R<String> delete(@RequestParam("ids") List<Long> ids) {
        setmealService.removeWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 根据id返回套餐信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        SetmealDto byIdWithDish = setmealService.getByIdWithDish(id);
        return R.success(byIdWithDish);
    }


    /**
     * 修改套餐信息
     *
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        //添加到数据库
        setmealService.updateWithDish(setmealDto);
        return R.success("套餐修改成功");
    }


    /**
     * 对套餐进行批量停售或单个停售
     *
     * @return
     */
    @PostMapping("/status/{status}")
    //这个参数这里一定记得加注解才能获取到参数，否则这里非常容易出问题
    public R<String> status(@PathVariable("status") Integer status, @RequestParam List<Long> ids) {
        //判断是否有值传入
//        log.info("status:{}", status);
//        log.info("ids:{}", ids);
        //根据id查询dish对象
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //设置等值查询
        queryWrapper.in(ids != null, Setmeal::getId, ids);
        //进行查询
        List<Setmeal> list = setmealService.list(queryWrapper);
        //遍历对象，并将其状态转为停售
        for (Setmeal setmeal : list) {
            setmeal.setStatus(status);
            //更新
            setmealService.updateById(setmeal);
        }

        return R.success("执行停售成功");
    }

    /**
     * 根据条件查询套餐数据
     *
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus() != null, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);

    }


}
