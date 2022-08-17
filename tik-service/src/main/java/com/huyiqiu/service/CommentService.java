package com.huyiqiu.service;


import com.huyiqiu.bo.CommentBO;
import com.huyiqiu.pojo.Comment;
import com.huyiqiu.utils.PagedGridResult;
import com.huyiqiu.vo.CommentVO;


public interface CommentService {

    /*
    * 发表评论
    * */
    public CommentVO createComment(CommentBO commentBO);

    /*
     * 查询评论列表
     * */
    public PagedGridResult getCommentList(String vlogId, String userId, Integer page, Integer pageSize);

    /*
     * 删除评论
     * */
    public void deleteComment(String commentUserId, String commentId, String vlogId);


    /*
    *根据主键查评论
     */
    public Comment getComment(String commentId);




}
