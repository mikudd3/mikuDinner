package org.mikudd3.reggie.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mikudd3.reggie.entity.AddressBook;

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
