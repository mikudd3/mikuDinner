package org.mikudd3.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mikudd3.reggie.entity.OrderDetail;

@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail> {
}
