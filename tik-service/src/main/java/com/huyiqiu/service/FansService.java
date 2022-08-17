package com.huyiqiu.service;


import com.huyiqiu.utils.PagedGridResult;

public interface FansService {

    // 关注用户
    public void doFollow(String myId, String vlogerId);

    // 取关用户
    public void doCancel(String myId, String vlogerId);

    // 判断是否关注
    public boolean queryIfFollow(String myId, String vlogerId);

    // 查询关注列表
    public PagedGridResult queryFollowList(String myId,
                                           Integer page,
                                           Integer pageSize);

    // 查询粉丝列表
    public PagedGridResult queryFansList(String myId,
                                         Integer page,
                                         Integer pageSize);


    //
}
