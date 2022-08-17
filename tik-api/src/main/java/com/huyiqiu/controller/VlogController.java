package com.huyiqiu.controller;


import com.huyiqiu.base.BaseInfoProperties;
import com.huyiqiu.bo.VlogBO;
import com.huyiqiu.enums.YesOrNo;
import com.huyiqiu.grace.result.GraceJSONResult;
import com.huyiqiu.service.VlogService;
import com.huyiqiu.utils.PagedGridResult;
import com.huyiqiu.vo.IndexVlogVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@Api(tags = "VlogController 短视频业务功能接口")
@RestController
@RequestMapping("vlog")

public class VlogController extends BaseInfoProperties {

//    @Value("${nacos.counts}")
//    private String nacosCounts;

    @Autowired
    private VlogService vlogService;

    @RequestMapping("publish")
    public GraceJSONResult publish(@RequestBody VlogBO vlogBO){

        // TODO 校验VlogBO
        vlogService.createVlog(vlogBO);
        return GraceJSONResult.ok();

    }

    @GetMapping("indexList")
    public GraceJSONResult indexList(@RequestParam(defaultValue = "", required = false) String search,
                                     @RequestParam(defaultValue = "", required = false) String userId,
                                     @RequestParam Integer page,
                                     @RequestParam Integer pageSize){

        if(page == null) page = COMMON_START_PAGE;
        if(pageSize == null) pageSize = COMMON_PAGE_SIZE;


        PagedGridResult list = vlogService.getIndexVlogList(search, userId, page, pageSize);

        return GraceJSONResult.ok(list);

    }

    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam String userId,
                                  @RequestParam String vlogId){


        IndexVlogVO vlogVO = vlogService.getVlogDetailById(userId, vlogId);

        return GraceJSONResult.ok(vlogVO);

    }

    @PostMapping("changeToPrivate")
    public GraceJSONResult changeToPrivate(@RequestParam String userId,
                                           @RequestParam String vlogId){


        vlogService.changeToPrivateOrPublic(vlogId, userId, YesOrNo.YES.type);

        return GraceJSONResult.ok();

    }

    @PostMapping("changeToPublic")
    public GraceJSONResult changeToPublic(@RequestParam String userId,
                                          @RequestParam String vlogId){


        vlogService.changeToPrivateOrPublic(vlogId, userId, YesOrNo.NO.type);

        return GraceJSONResult.ok();

    }

    @GetMapping("myPublicList")
    public GraceJSONResult myPublicList(@RequestParam String userId,
                                        @RequestParam Integer page,
                                        @RequestParam Integer pageSize){

        if(page == null) page = COMMON_START_PAGE;
        if(pageSize == null) pageSize = COMMON_PAGE_SIZE;

        PagedGridResult list = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.NO.type);


        return GraceJSONResult.ok(list);

    }

    @GetMapping("myPrivateList")
    public GraceJSONResult myPrivateList(@RequestParam String userId,
                                         @RequestParam Integer page,
                                         @RequestParam Integer pageSize){

        if(page == null) page = COMMON_START_PAGE;
        if(pageSize == null) pageSize = COMMON_PAGE_SIZE;

        PagedGridResult list = vlogService.queryMyVlogList(userId, page, pageSize, YesOrNo.YES.type);

        return GraceJSONResult.ok(list);

    }

    @GetMapping("myLikedList")
    public GraceJSONResult myLikedList(@RequestParam String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize){

        if(page == null) page = COMMON_START_PAGE;
        if(pageSize == null) pageSize = COMMON_PAGE_SIZE;

        PagedGridResult list = vlogService.queryMyLikedVlogList(userId, page, pageSize);
        return GraceJSONResult.ok(list);
    }

    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,
                                @RequestParam String vlogId,
                                @RequestParam String vlogerId){

        vlogService.userLikeVlog(userId, vlogId);
        //redis 计数
        redis.increment(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.increment(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);

        //保存关联关系
        redis.set(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId, "1");

        String cntStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId);
        log.info("==================" + cntStr + "==================");
//        Integer cnt = 0;
//        if (StringUtils.isNotBlank(cntStr)){
//            cnt = Integer.valueOf(cntStr);
//            int nacosc = Integer.valueOf(nacosCounts);
//            if(cnt % nacosc == 0){ //
//                vlogService.refreshLike(vlogId, cnt);
//            }
//        }
        
        // 点赞完毕，获得redis中的总数
        return GraceJSONResult.ok();

    }

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                  @RequestParam String vlogId,
                                  @RequestParam String vlogerId){

        // 取消点赞
        vlogService.userUnlikeVlog(userId, vlogId);
        //redis 计数
        redis.decrement(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId, 1);
        redis.decrement(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + vlogerId, 1);

        //保存关联关系
        redis.del(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId);

        return GraceJSONResult.ok();

    }

    @PostMapping("totalLikedCounts")
    public GraceJSONResult totalLikedCounts(@RequestParam String vlogId){

        return GraceJSONResult.ok(vlogService.getVlogBeLikedCounts(vlogId));
    }

    @GetMapping("followList")
    public GraceJSONResult getMyFollowVlogList(@RequestParam String myId,
                                               @RequestParam Integer page,
                                               @RequestParam Integer pageSize){

        if(page == null) page = COMMON_START_PAGE;
        if(pageSize == null) pageSize = COMMON_PAGE_SIZE;

        PagedGridResult list = vlogService.getMyFollowVlogList(myId, page, pageSize);

        return GraceJSONResult.ok(list);
    }

    @GetMapping("friendList")
    public GraceJSONResult getMyFriendVlogList(@RequestParam String myId,
                                               @RequestParam Integer page,
                                               @RequestParam Integer pageSize){

        if(page == null) page = COMMON_START_PAGE;
        if(pageSize == null) pageSize = COMMON_PAGE_SIZE;

        PagedGridResult list = vlogService.getMyFriendVlogList(myId, page, pageSize);

        return GraceJSONResult.ok(list);
    }

}
