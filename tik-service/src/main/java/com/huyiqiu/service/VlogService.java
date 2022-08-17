package com.huyiqiu.service;

import com.huyiqiu.bo.VlogBO;
import com.huyiqiu.pojo.Vlog;
import com.huyiqiu.utils.PagedGridResult;
import com.huyiqiu.vo.IndexVlogVO;


public interface VlogService {

    // 新增vlog视频
    public void createVlog(VlogBO vlogBO);

    // 查询vlog
    public PagedGridResult getIndexVlogList(String search, String UserId, Integer page, Integer pageSize);

    // 根据主键查看vlog细节 // 设置全局主键 sid
    public IndexVlogVO getVlogDetailById(String userId, String vlogId);

    // 用户修改视频访问权限
    public void changeToPrivateOrPublic(String vlogId, String userId, Integer yesOrNo);

    // 查看自己的vlog
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo);

    // 用户点赞
    public void userLikeVlog(String userId, String vlogId);

    // 点赞数入库
    public void refreshLike(String vlogId, Integer counts);

    // 根据vlogId查询Vlog信息
    public Vlog getVlog(String vlogId);

    // 取消点赞
    public void userUnlikeVlog(String userId, String vlogId);

    // ..
    public Integer getVlogBeLikedCounts(String vlogId);

    //查询我点赞过的vlog
    public PagedGridResult queryMyLikedVlogList(String userId, Integer page, Integer pageSize);

    // 查看我关注的博主的视频
    public PagedGridResult getMyFollowVlogList(String userId, Integer page, Integer pageSize);

    // 查询朋友列表
    public PagedGridResult getMyFriendVlogList(String userId, Integer page, Integer pageSize);

}
