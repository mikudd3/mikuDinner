package org.mikudd3.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.mikudd3.reggie.common.R;
import org.mikudd3.reggie.entity.User;
import org.mikudd3.reggie.service.UserService;
import org.mikudd3.reggie.util.SMSUtils;
import org.mikudd3.reggie.util.ValidateCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @project:
 * @author: mikudd3
 * @version: 1.0
 */

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;


    /**
     * 发送短信
     *
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();

        //如果手机号不为空
        if (StringUtils.isNotEmpty(phone)) {
            //生成4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            //输出验证码用于测试
            log.info("code={}", code);
            //调用短信服务发送短信
            //SMSUtils.sendMessage("mikudd3", "", phone, code);

            //将短信保持到session
            session.setAttribute(phone, code);
            return R.success("短信发送成功");
        }

        return R.error("发送失败");
    }

    /**
     * 移动端用户登录
     *
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        //测试是否传值
        log.info(map.toString());

        //获取手机号
        String phone = (String) map.get("phone");
        //获取验证码
        String code = (String) map.get("code");
        //从session获取保持的验证码
        String sessionCode = (String) session.getAttribute(phone);
        //进行验证码比较
        if (sessionCode != null && sessionCode.equals(code)) {
            //登录成功
            //判断当前手机号是否为新用户,如果为新用户则自动创建账号
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            //根据电话查询用户是否存在
            queryWrapper.eq(User::getPhone, phone);
            //执行查询
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                //新用户，则在数据库中创建该用户
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            return R.success(user);
        }

        return R.error("登录失败");
    }
}
