package com.huyiqiu.controller;

import com.huyiqiu.grace.result.GraceJSONResult;
import com.huyiqiu.utils.SMSUtils;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = "Hello 测试接口")
@RestController
public class HelloController {

    @Autowired
    private SMSUtils smsUtils;

    @GetMapping("hello")
    public GraceJSONResult hello(){
        return GraceJSONResult.ok("hello");
    }

    @GetMapping("testSMS")
    public GraceJSONResult testSMS() throws Exception {
        String code = "123456";
        smsUtils.sendSMS("18351817575", code);

        return GraceJSONResult.ok();
    }
}
