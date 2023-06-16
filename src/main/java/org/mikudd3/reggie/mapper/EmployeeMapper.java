package org.mikudd3.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.mikudd3.reggie.entity.Employee;

/**
 * @project: 员工mapper
 * @author: mikudd3
 * @version: 1.0
 */

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
