package org.mikudd3.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.mikudd3.reggie.entity.Orders;

/**
 * 订单service
 */
public interface OrdersService extends IService<Orders> {

    /**
     * 用户下单
     *
     * @param orders
     */
    public void submit(Orders orders);
}
