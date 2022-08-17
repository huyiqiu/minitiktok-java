package com.huyiqiu.controller;


import com.huyiqiu.base.BaseInfoProperties;
import com.huyiqiu.grace.result.GraceJSONResult;
import com.huyiqiu.grace.result.ResponseStatusEnum;
import com.huyiqiu.service.FansService;
import com.huyiqiu.service.UserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Api(tags = "FansController 粉丝相关业务功能接口")
@RestController
@RequestMapping("fans")
public class FansController extends BaseInfoProperties {

    @Autowired
    private FansService fansService;
    @Autowired
    private UserService userService;

    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String myId,
                                  @RequestParam String vlogerId) {
        // id 不空判断
        if(StringUtils.isBlank(myId) || StringUtils.isBlank(vlogerId))
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        // id 对应用户存在判断
        if(userService.getUser(myId) == null || userService.getUser(vlogerId) == null)
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
        // 不能自己关注自己
        if(myId.equalsIgnoreCase(vlogerId))
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);

        fansService.doFollow(myId, vlogerId);

        // TODO 思考是否应该将操作redis的过程放到service层
        //博主粉丝+1， 我的关注+1
        redis.increment(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.increment(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        //我和博主的关联关系也可以依赖redis， 避免db性能瓶颈
        redis.set(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":" + myId + ":" + vlogerId, "1");


        return GraceJSONResult.ok();
    }

    @PostMapping("cancel")
    public GraceJSONResult cancel(@RequestParam String myId,
                                  @RequestParam String vlogerId) {


        fansService.doCancel(myId, vlogerId);

        // TODO 思考是否应该将操作redis的过程放到service层
        //博主粉丝-1， 我的关注-1
        redis.decrement(REDIS_MY_FOLLOWS_COUNTS + ":" + myId, 1);
        redis.decrement(REDIS_MY_FANS_COUNTS + ":" + vlogerId, 1);

        //我和博主的关联关系也可以依赖redis， 避免db性能瓶颈
        redis.del(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":" + myId + ":" + vlogerId);

        return GraceJSONResult.ok();
    }

    @GetMapping("queryDoIFollowVloger")
    public GraceJSONResult queryDoIFollowVloger(@RequestParam String myId,
                                                @RequestParam String vlogerId) {

        return GraceJSONResult.ok(fansService.queryIfFollow(myId, vlogerId));
    }

    @GetMapping("queryMyFollows")
    public GraceJSONResult queryMyFollows(@RequestParam String myId,
                                          @RequestParam Integer page,
                                          @RequestParam Integer pageSize) {

        if (page == null) page = COMMON_START_PAGE;
        if (pageSize == null) pageSize = COMMON_PAGE_SIZE;
        return GraceJSONResult.ok(fansService.queryFollowList(myId, page, pageSize));
    }

    @GetMapping("queryMyFans")
    public GraceJSONResult queryMyFans(@RequestParam String myId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) {

        if (page == null) page = COMMON_START_PAGE;
        if (pageSize == null) pageSize = COMMON_PAGE_SIZE;
        return GraceJSONResult.ok(fansService.queryFansList(myId, page, pageSize));
    }

}
