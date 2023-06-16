package org.mikudd3.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.mikudd3.reggie.entity.Employee;
import org.mikudd3.reggie.mapper.EmployeeMapper;
import org.mikudd3.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @project: 员工service实现
 * @author: mikudd3
 * @version: 1.0
 */

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
