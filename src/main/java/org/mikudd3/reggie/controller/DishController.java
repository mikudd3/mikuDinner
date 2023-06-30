package org.mikudd3.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.mikudd3.reggie.common.BaseContext;
import org.mikudd3.reggie.common.R;
import org.mikudd3.reggie.dto.DishDto;
import org.mikudd3.reggie.entity.*;
import org.mikudd3.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @project:
 * @author: mikudd3
 * @version: 1.0
 */

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info(String.valueOf(dishDto));
        //添加到数据库
        dishService.saveWithFlavor(dishDto);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }

    /**
     * 分页查询
     *
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加条件，如果传入名字不为空
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageInfo, queryWrapper);
        //对象拷贝,忽略records属性
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        //对records属性进行处理
        List<Dish> records = pageInfo.getRecords();
        //获取list
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //对属性进行拷贝
            BeanUtils.copyProperties(item, dishDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //获取分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //获取分类的名字
                String categoryName = category.getName();
                //将分类的名字放入到DishDto中
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }


    /**
     * 根据id返回菜品信息和对应的口味信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    /**
     * 修改菜品信息
     *
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info(String.valueOf(dishDto));
        //添加到数据库
        dishService.updateWithFlavor(dishDto);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("菜品修改成功");
    }

    /**
     * 对菜品批量或者是单个 进行停售或者是起售
     *
     * @return
     */
    @PostMapping("/status/{status}")
    //这个参数这里一定记得加注解才能获取到参数，否则这里非常容易出问题
    public R<String> status(@PathVariable("status") Integer status, @RequestParam List<Long> ids) {
        //判断是否有值传入
        log.info("status:{}", status);
        log.info("ids:{}", ids);
        //根据id查询dish对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //设置等值查询
        queryWrapper.in(ids != null, Dish::getId, ids);
        //进行查询
        List<Dish> list = dishService.list(queryWrapper);
        //遍历对象，并将其状态转为停售
        for (Dish dish : list) {
            dish.setStatus(status);
            //更新
            dishService.updateById(dish);
        }

        return R.success("执行停售成功");
    }


    @DeleteMapping
    public R<String> deleteByIds(@RequestParam("ids") List<Long> ids) {
        dishService.deleteByIds(ids);
        return R.success("删除成功");
    }

    /**
     * 根据条件查询对应菜品
     *
     * @param dish
     * @return
     */
    @GetMapping("list")
    public R<List<DishDto>> list(Dish dish) {
        //先从缓存中查询是否有数据
        List<DishDto> dishDtoList = null;
        //动态构造key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //先从redis中获取缓存数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果缓存中有数据，则将其返回
        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }
        //如果redis缓存中找不到该数据，则从数据库中查找
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //构建等值条件
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //查询在启售状态下的菜品
        queryWrapper.eq(Dish::getStatus, 1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //执行查询
        List<Dish> list = dishService.list(queryWrapper);

        //进行集合的泛型转换
        dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //为一个新的对象赋值，一定要考虑你为它赋过几个值，否则你自己都不知道就返回了null的数据
            //为dishDto对象的基本属性拷贝
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //为dishdto赋值flavors属性
            //当前菜品的id
            Long dishId = item.getId();
            //创建条件查询对象
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            //select * from dish_flavor where dish_id = ?
            //这里之所以使用list来条件查询那是因为同一个dish_id 可以查出不同的口味出来,就是查询的结果不止一个
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，需要查询数据库，将查询到的菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}
