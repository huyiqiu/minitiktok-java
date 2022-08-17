package com.huyiqiu.controller;

import com.huyiqiu.base.BaseInfoProperties;
import com.huyiqiu.bo.RegistLoginBO;
import com.huyiqiu.grace.result.GraceJSONResult;
import com.huyiqiu.grace.result.ResponseStatusEnum;
import com.huyiqiu.pojo.Users;
import com.huyiqiu.service.UserService;
import com.huyiqiu.utils.IPUtil;
import com.huyiqiu.utils.MyInfo;
import com.huyiqiu.utils.SMSUtils;
import com.huyiqiu.vo.UsersVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.UUID;


@Slf4j
@Api(tags = "PassportController 用户验证接口")
@RestController
@RequestMapping("passport")
public class PassportController extends BaseInfoProperties {

    @ApiOperation(value = "hello 测试路由")
    @GetMapping("/demo")
    public Object hello(){
        return GraceJSONResult.ok("HelloSpringBoot~~~");
    }


    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "测试腾讯云")
    @GetMapping("/sms")
    public Object send() throws Exception {
        String code = "123456";
        smsUtils.sendSMS(MyInfo.getMobile(), code);

        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "连接前端发送验证码~")
    @PostMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile,
                                      HttpServletRequest request) throws Exception {

        if(StringUtils.isBlank(mobile)){
            return GraceJSONResult.ok();
        }
        log.info("hello...");
        // TODO 获得用户IP，
        String userIp = IPUtil.getRequestIp(request);

        // TODO 限制用户在60s之内只能获得一次验证码
        redis.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);

        String code = (int)((Math.random() * 9 + 1) * 100000) + "";
        smsUtils.sendSMS(mobile, code);

        log.info(code);
        // TODO 把验证码放入redis中，用于后续的验证
        redis.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);

        return GraceJSONResult.ok();
    }

    @ApiOperation(value = "用户一键登录/注册接口")
    @PostMapping("login")
    public GraceJSONResult doLogin(@Valid @RequestBody RegistLoginBO registLoginBO,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {

        String mobile = registLoginBO.getMobile();
        String code = registLoginBO.getSmsCode();

        // 1. 从redis中取验证码 匹配
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if(StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(code)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2. 查询数据库，判断用户是否存在
        Users user = userService.queryMobileIsExist(mobile);

        if(user == null) userService.createUser(mobile); // 若不存在则创建

        // 3. 如果不为空， 可以继续业务， 可以保存用户会话信息
        String uToken = UUID.randomUUID().toString();
        redis.set(REDIS_USER_TOKEN + ":" + user.getId(), uToken);

        // 4. 用户登录注册成功后， 删除redis中的短信验证码
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        // 5. 返回用户信息，包括token令牌
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setUserToken(uToken);

        return GraceJSONResult.ok(usersVO);

    }

    @PostMapping("logout")
    public GraceJSONResult logout(@RequestParam String userId,
                                  HttpServletRequest request) throws Exception{

        // 后端清除token信息即可， 前端清除本地app中的用户信息和token会话信息
        redis.del(REDIS_USER_TOKEN + ":" + userId);


        return GraceJSONResult.ok();
    }

}
