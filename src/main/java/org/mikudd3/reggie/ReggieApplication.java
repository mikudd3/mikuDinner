package org.mikudd3.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @project:
 * @author: mikudd3
 * @version: 1.0
 */

@Slf4j //日志
@SpringBootApplication //springboot启动
@ServletComponentScan //扫描过滤器
@EnableTransactionManagement//开启事务
public class ReggieApplication {
    public static void main(String[] args) {
        //启动springboot
        SpringApplication.run(ReggieApplication.class, args);
        log.info("项目启动成功");
    }
}
