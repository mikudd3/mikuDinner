package org.mikudd3.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.mikudd3.reggie.common.R;
import org.mikudd3.reggie.entity.Orders;
import org.mikudd3.reggie.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @project:
 * @author: mikudd3
 * @version: 1.0
 */

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime) {
        //分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //设置查询条件
        queryWrapper.like(number != null, Orders::getNumber, number)
                .gt(StringUtils.isNotEmpty(beginTime), Orders::getOrderTime, beginTime)
                .lt(StringUtils.isNotEmpty(endTime), Orders::getOrderTime, endTime);

        //执行查询
        ordersService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }


    /**
     * 用户下单
     *
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submit(orders);
        return R.success("下单成功");
    }


    /**
     * 用户订单分页查询
     *
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page, int pageSize) {

        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    @PutMapping()
    public R update(@RequestBody Orders orders) {
        //根据id更新数据
        ordersService.updateById(orders);
        return R.success("状态更新成功");
    }


}
