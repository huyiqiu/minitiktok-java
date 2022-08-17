package com.huyiqiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.huyiqiu.base.BaseInfoProperties;
import com.huyiqiu.bo.CommentBO;
import com.huyiqiu.enums.MessageEnum;
import com.huyiqiu.enums.YesOrNo;
import com.huyiqiu.mapper.CommentMapper;
import com.huyiqiu.mapper.CommentMapperCustom;
import com.huyiqiu.pojo.Comment;
import com.huyiqiu.pojo.Vlog;
import com.huyiqiu.service.CommentService;
import com.huyiqiu.service.MsgService;
import com.huyiqiu.service.VlogService;
import com.huyiqiu.utils.PagedGridResult;
import com.huyiqiu.vo.CommentVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl extends BaseInfoProperties implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentMapperCustom commentMapperCustom;

    @Autowired
    private MsgService msgService;

    @Autowired
    private VlogService vlogService;

    @Autowired
    private Sid sid;

    @Override
    public CommentVO createComment(CommentBO commentBO) {
        Comment comment = new Comment();

        comment.setId(sid.nextShort());
        comment.setVlogId(commentBO.getVlogId());
        comment.setVlogerId(commentBO.getVlogerId());
        comment.setCommentUserId(commentBO.getCommentUserId());
        comment.setFatherCommentId(commentBO.getFatherCommentId());
        comment.setContent(commentBO.getContent());

        comment.setLikeCounts(0);
        comment.setCreateTime(new Date());

        commentMapper.insert(comment);

        // redis 操作放在service中
        redis.increment(REDIS_VLOG_COMMENT_COUNTS + ":" + commentBO.getVlogId() + ":", 1);

        // 留言后的最新评论需要返回给前端展示
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(comment, commentVO);

        // 系统消息， 评论/回复 通知
        Vlog vlog = vlogService.getVlog(commentBO.getVlogId());
        Map msgContent = new HashMap<>();
        msgContent.put("vlogCover", vlog.getCover());
        msgContent.put("vlogId", commentBO.getVlogId());
        msgContent.put("commentId", comment.getId());
        msgContent.put("commentContent", commentBO.getContent());

        Integer type = MessageEnum.COMMENT_VLOG.type;
        if(StringUtils.isNotBlank(commentBO.getFatherCommentId()) &&
                !commentBO.getFatherCommentId().equalsIgnoreCase("0"))
        {
            type = MessageEnum.REPLY_YOU.type;

        }
        msgService.createMsg(commentBO.getCommentUserId(), commentBO.getVlogerId(), type, msgContent);

        return commentVO;

    }

    // 获得评论列表
    @Override
    public PagedGridResult getCommentList(String vlogId, String userId, Integer page, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);
        PageHelper.startPage(page, pageSize);
        List<CommentVO> comments = commentMapperCustom.getCommentList(map);

        for(CommentVO comment : comments){
            // 某个评论的点赞数
            String countsStr = redis.getHashValue(REDIS_VLOG_COMMENT_LIKED_COUNTS, comment.getCommentId());
            Integer cnt = 0;
            if(StringUtils.isNotBlank(countsStr)){
                cnt = Integer.valueOf(countsStr);
            }
            comment.setLikeCounts(cnt);

            // 判断评论是否被我点赞过
            String hashValue = redis.getHashValue(REDIS_USER_LIKE_COMMENT, userId + ":" +comment.getCommentId());
            if(StringUtils.isNotBlank(hashValue) && hashValue.equalsIgnoreCase("1")){
                comment.setIsLike(YesOrNo.YES.type);
            }
        }
        return setterPagedGrid(comments, page);
    }

    // 长按 删除评论
    @Override
    public void deleteComment(String commentUserId, String commentId, String vlogId) {
        Comment pendingComment = new Comment();
        pendingComment.setId(commentId);
        pendingComment.setCommentUserId(commentUserId);

        commentMapper.delete(pendingComment);

        redis.decrement(REDIS_VLOG_COMMENT_COUNTS + ":" + vlogId + ":", 1);

    }

    @Override
    public Comment getComment(String commentId) {
        return commentMapper.selectByPrimaryKey(commentId);
    }

}
