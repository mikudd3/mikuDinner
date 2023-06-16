package org.mikudd3.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.mikudd3.reggie.common.BaseContext;
import org.mikudd3.reggie.common.R;
import org.mikudd3.reggie.entity.Employee;
import org.mikudd3.reggie.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @project: 员工Controller
 * @author: mikudd3
 * @version: 1.0
 */

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired //使用自动装配创建EmployeeService
    private EmployeeService service;

    /**
     * 登录实现
     *
     * @param request  传入request对象，用于获取session并保存登录信息
     * @param employee 前端传入的数据为json数据，使用@RequestBody接收并封装为对象
     * @return
     */
    @PostMapping("/login")
    public R login(HttpServletRequest request, @RequestBody Employee employee) {

        //1。将页面提交的密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2.根据用户名查询数据
        //2.1创建LambdaQueryWrapper对象
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //2.2进行等值查询
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        //2.3进行查询
        Employee emp = service.getOne(queryWrapper);

        //3.如果没有查询到该用户则返回结果
        if (emp == null) {
            return R.error("登录失败");
        }

        //4.如果查到用户则进行密码比对
        if (!emp.getPassword().equals(password)) {
            //如果密码不正确
            return R.error("登录失败，密码错误");
        }

        //5.查看员工状态
        if (emp.getStatus() == 0) {
            return R.error("登录失败，账号已禁用");
        }

        //6.登录成功，则将结果封装为session并返回
        request.getSession().setAttribute("employee", emp.getId());

        return R.success(emp);

    }

    /**
     * 员工退出功能实现
     *
     * @param request
     * @return
     */
    @RequestMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        //清理session中保存的id信息
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    @RequestMapping
    public R<String> save(@RequestBody Employee employee) {
        log.info("新增员工信息 ：{}", employee.toString());
        //1.设置初始密码
        //1.1对初始秘密进行加密
        String password = DigestUtils.md5DigestAsHex("123456".getBytes());
        employee.setPassword(password);
        //保存用户,添加用户
        service.save(employee);

        return R.success("新增员工成功");
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
        //判断数据是否已经传入
        log.info("输入数据: page={}, pageSize = {},name = {}", page, pageSize, name);

        //构造分页构造器
        Page pageInfo = new Page<>(page, pageSize);

        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件，如果传入名字不为空
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        service.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 更新员工信息

     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee) {
        //执行更新操作
        service.updateById(employee);

        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        //根据id查询员工
        Employee emp = service.getById(id);
        if (emp != null) {
            return R.success(emp);
        }
        return R.error("没有查询到对应员工信息");
    }


}
