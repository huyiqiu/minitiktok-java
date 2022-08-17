package com.huyiqiu.controller;


import com.huyiqiu.base.BaseInfoProperties;
import com.huyiqiu.bo.CommentBO;
import com.huyiqiu.enums.MessageEnum;
import com.huyiqiu.grace.result.GraceJSONResult;
import com.huyiqiu.pojo.Comment;
import com.huyiqiu.pojo.Vlog;
import com.huyiqiu.service.CommentService;
import com.huyiqiu.service.MsgService;
import com.huyiqiu.service.VlogService;
import com.huyiqiu.utils.PagedGridResult;
import com.huyiqiu.vo.CommentVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Api("CommentController 评论模块功能接口")
@RestController
@RequestMapping("comment")
public class CommentController extends BaseInfoProperties {

    @Autowired
    private CommentService commentService;

    @Autowired
    private MsgService msgService;

    @Autowired
    private VlogService vlogService;

    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO commentBO) throws Exception{


        CommentVO commentVO = commentService.createComment(commentBO);

        return GraceJSONResult.ok(commentVO);
    }

    @GetMapping("counts")
    public GraceJSONResult counts(@RequestParam String vlogId) throws Exception{

        String nums = redis.get(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId);
        if(StringUtils.isBlank(nums)) nums = "0";
        return GraceJSONResult.ok(Integer.valueOf(nums));
    }

    @GetMapping("list")
    public GraceJSONResult commentList(@RequestParam String vlogId,
                                       @RequestParam(defaultValue = "") String userId,
                                       @RequestParam Integer page,
                                       @RequestParam Integer pageSize) throws Exception{

        if(page == null) page = COMMON_START_PAGE;
        if(pageSize == null) pageSize = COMMON_PAGE_SIZE;
        PagedGridResult commentList = commentService.getCommentList(vlogId, userId, page, pageSize);

        return GraceJSONResult.ok(commentList);
    }


    @DeleteMapping("delete")
    public GraceJSONResult delete(@RequestParam String commentUserId,
                                  @RequestParam String commentId,
                                  @RequestParam String vlogId) throws Exception{

        commentService.deleteComment(commentUserId, commentId, vlogId);

        return GraceJSONResult.ok();
    }

    @PostMapping("like")
    public GraceJSONResult like(@RequestParam String userId,
                                @RequestParam String commentId) throws Exception{

        // big key 需要避免
        redis.incrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
        redis.setHashValue(REDIS_USER_LIKE_COMMENT, userId + ":" +commentId, "1");

        // 系统消息 ： 点赞评论
        Comment comment = commentService.getComment(commentId);
        Vlog vlog = vlogService.getVlog(comment.getVlogId());
        Map msgContent = new HashMap<>();
        msgContent.put("commentId", commentId);
        msgContent.put("vlogId", vlog.getId());
        msgContent.put("vlogCover", vlog.getCover());
        msgService.createMsg(userId, comment.getCommentUserId(), MessageEnum.LIKE_COMMENT.type, msgContent);

        return GraceJSONResult.ok();
    }

    @PostMapping("unlike")
    public GraceJSONResult unlike(@RequestParam String userId,
                                  @RequestParam String commentId) throws Exception{

        redis.decrementHash(REDIS_VLOG_COMMENT_LIKED_COUNTS, commentId, 1);
        redis.hdel(REDIS_USER_LIKE_COMMENT, userId + ":" +commentId);

        return GraceJSONResult.ok();
    }
}
