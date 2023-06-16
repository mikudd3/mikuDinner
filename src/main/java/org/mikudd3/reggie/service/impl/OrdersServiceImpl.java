package org.mikudd3.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mikudd3.reggie.common.BaseContext;
import org.mikudd3.reggie.common.CustomException;
import org.mikudd3.reggie.entity.*;
import org.mikudd3.reggie.mapper.OrdersMapper;
import org.mikudd3.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @project:
 * @author: mikudd3
 * @version: 1.0
 */

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    OrderDetailService orderDetailService;

    /**
     * 用户下单
     *
     * @param orders
     */
    @Override
    @Transactional
    public void submit(Orders orders) {
        //前端请求携带的参数是没有用户id的,所以要获取用户id
        Long userId = BaseContext.getCurrentId();
        //创建查询条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        //查询当前用户的购物车数据
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空,不能下单");
        }
        //查询用户数据
        User user = userService.getById(userId);
        //查询用户地址
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            throw new CustomException("地址信息有误,不能下单");
        }

        Long orderId = IdWorker.getId();//使用工具生成订单号
        orders.setId(orderId);

        //进行购物车的金额数据计算 顺便把订单明细给计算出来
        AtomicInteger amount = new AtomicInteger(0);//使用原子类来保存计算的金额结果
        //这个item是集合中的每一个shoppingCarts对象,是在变化的
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            //每对item进行一次遍历就产生一个新的orderDetail对象,然后对orderDetail进行设置,然后返回被收集,被封装成一个集合
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());//单份的金额
            //addAndGet进行累加 item.getAmount()单份的金额  multiply乘  item.getNumber()份数
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;

        }).collect(Collectors.toList());

        //向订单插入数据,一条数据  因为前端传过来的数据太少了,所以我们需要对相关的属性进行填值

        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        //Amount是指订单总的金额
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        if (user.getName() != null) {
            orders.setUserName(user.getName());
        }
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        this.save(orders);


        //先明细表插入数据,多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据  queryWrapper封装了userId我们直接使用这个条件来进行删除就行
        shoppingCartService.remove(queryWrapper);
    }


}
