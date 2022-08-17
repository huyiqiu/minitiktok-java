package com.huyiqiu.service;

import com.huyiqiu.bo.UpdateUserBO;
import com.huyiqiu.pojo.Users;


public interface UserService {

    /**
     * 判断用户是否存在
     */
    public Users queryMobileIsExist(String mobile);

    /**
     * 新建用户
     */
    public Users createUser(String mobile);

    /**
     * 根据用户主键查询用户信息
     */
    public Users getUser(String userId);

    /**
     * 用户信息修改
     */
    public Users updateUserInfo(UpdateUserBO updateUserBO);

    /**
     * 用户信息修改
     */
    public Users updateUserInfo(UpdateUserBO updateUserBO, Integer type);

}
