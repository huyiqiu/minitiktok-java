package com.huyiqiu.service.impl;

import com.huyiqiu.bo.UpdateUserBO;
import com.huyiqiu.enums.Sex;
import com.huyiqiu.enums.UserInfoModifyType;
import com.huyiqiu.enums.YesOrNo;
import com.huyiqiu.exceptions.GraceException;
import com.huyiqiu.grace.result.ResponseStatusEnum;
import com.huyiqiu.mapper.UsersMapper;
import com.huyiqiu.pojo.Users;
import com.huyiqiu.service.UserService;
import org.n3r.idworker.Sid;
import org.n3r.idworker.utils.DateUtil;
import org.n3r.idworker.utils.DesensitizationUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;
    private static final String USER_FACE1 = "http://47.113.185.142:9000/faketik/displayPic.jpg";


    @Override
    public Users queryMobileIsExist(String mobile) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("mobile", mobile);

        Users user = usersMapper.selectOneByExample(userExample);
        return user;
    }

    @Transactional
    @Override
    public Users createUser(String mobile) {

        //全局唯一主键
        String userId = sid.nextShort();

        Users user = new Users();
        user.setId(userId);

        user.setMobile(mobile);
        user.setNickname("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setImoocNum("用户：" + DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE1);

        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");
        user.setDescription("这家伙很懒，什么都没留下~");
        user.setCanImoocNumBeUpdated(YesOrNo.YES.type);

        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());

        usersMapper.insert(user);


        return user;
    }

    @Override
    public Users getUser(String userId) {

        Users user = usersMapper.selectByPrimaryKey(userId);

        return user;
    }

    @Transactional
    @Override
    public Users updateUserInfo(UpdateUserBO updateUserBO) {
        Users pendingUser = new Users();
        BeanUtils.copyProperties(updateUserBO, pendingUser);

        int result = usersMapper.updateByPrimaryKeySelective(pendingUser);
        if(result != 1) GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        return getUser(updateUserBO.getId()); // ?
    }

    @Override
    public Users updateUserInfo(UpdateUserBO updateUserBO, Integer type) {
        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();

        if(type == UserInfoModifyType.NICKNAME.type){
            criteria.andEqualTo("nickname", updateUserBO.getNickname());
            Users user = usersMapper.selectOneByExample(example);
            if(user != null){
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_NICKNAME_EXIST_ERROR);
            }
        }
        if(type == UserInfoModifyType.IMOOCNUM.type){
            criteria.andEqualTo("imoocNum", updateUserBO.getImoocNum());
            Users user = usersMapper.selectOneByExample(example);
            if(user != null){
                GraceException.display(ResponseStatusEnum.USER_INFO_UPDATED_IMOOCNUM_EXIST_ERROR);
            }
            if(getUser(updateUserBO.getId()).getCanImoocNumBeUpdated() == YesOrNo.NO.type){
                GraceException.display(ResponseStatusEnum.USER_INFO_CANT_UPDATED_IMOOCNUM_ERROR);
            }
            updateUserBO.setCanImoocNumBeUpdated(YesOrNo.NO.type);
        }
        return updateUserInfo(updateUserBO);
    }
}
