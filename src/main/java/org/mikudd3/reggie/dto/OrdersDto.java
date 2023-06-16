package org.mikudd3.reggie.dto;


import lombok.Data;
import org.mikudd3.reggie.entity.OrderDetail;
import org.mikudd3.reggie.entity.Orders;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
