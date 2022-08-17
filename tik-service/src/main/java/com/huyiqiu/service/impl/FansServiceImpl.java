package com.huyiqiu.service.impl;


import com.github.pagehelper.PageHelper;
import com.huyiqiu.base.BaseInfoProperties;
import com.huyiqiu.enums.MessageEnum;
import com.huyiqiu.enums.YesOrNo;
import com.huyiqiu.mapper.FansMapper;
import com.huyiqiu.mapper.FansMapperCustom;
import com.huyiqiu.pojo.Fans;
import com.huyiqiu.service.FansService;
import com.huyiqiu.service.MsgService;
import com.huyiqiu.utils.PagedGridResult;
import com.huyiqiu.vo.FansVO;
import com.huyiqiu.vo.VlogerVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FansServiceImpl extends BaseInfoProperties implements FansService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private FansMapperCustom fansMapperCustom;

    @Autowired
    private Sid sid;

    @Autowired
    private MsgService msgService;


    @Transactional
    @Override
    public void doFollow(String myId, String vlogerId) {


        String fid = sid.nextShort();
        Fans fans = new Fans();
        fans.setId(fid);
        fans.setFanId(myId);
        fans.setVlogerId(vlogerId);

        // 判断是否相互关注
        Fans vloger = queryFansRelationship(vlogerId, myId);
        if(vloger != null) {
            fans.setIsFanFriendOfMine(YesOrNo.YES.type);
            vloger.setIsFanFriendOfMine(YesOrNo.YES.type);
            fansMapper.updateByPrimaryKeySelective(vloger); // ?
        }else{
            fans.setIsFanFriendOfMine(YesOrNo.NO.type);
        }
        fansMapper.insert(fans);

        // 发送关注系统消息
        msgService.createMsg(myId, vlogerId, MessageEnum.FOLLOW_YOU.type, null);
    }

    public Fans queryFansRelationship(String fanId, String vlogerId){
        Example example = new Example(Fans.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("fanId", fanId); // ?
        criteria.andEqualTo("vlogerId", vlogerId); // ?

        Fans fan = null;
        List<Fans> fans = fansMapper.selectByExample(example);

        if (fans != null && fans.size() > 0){
            fan = fans.get(0);
        }

        return fan;
    }

    @Transactional
    @Override
    public void doCancel(String myId, String vlogerId) {
        Fans fan = queryFansRelationship(myId, vlogerId);
        if(fan != null && fan.getIsFanFriendOfMine() == YesOrNo.YES.type){
            Fans vloger = queryFansRelationship(myId, vlogerId);
            vloger.setIsFanFriendOfMine(YesOrNo.NO.type);
            fansMapper.updateByPrimaryKeySelective(vloger);
        }

        // 删除表记录
        fansMapper.delete(fan);

    }

    @Override
    public boolean queryIfFollow(String myId, String vlogerId) {
        return redis.get(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":" + myId + ":" + vlogerId) != null;
//        Fans fan = queryFansRelationship(myId, vlogerId);
//        return fan != null;
    }

    @Override
    public PagedGridResult queryFollowList(String myId, Integer page, Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        List<VlogerVO> vlogers = fansMapperCustom.queryMyFollows(map);

        return setterPagedGrid(vlogers, page);
    }

    @Override
    public PagedGridResult queryFansList(String myId, Integer page, Integer pageSize) {

        /*
        * 判断粉丝是否和我互关
        * 传统做法： 多表关联 + 嵌套关联查询，高并发下容易出现性能问题
        *
        * 常规做法：
        * 1.避免过多的表关联查询，先查询粉丝列表，获得fanList
        * 2.判断是否互相关注 ->遍历fanList，查询我是否关注
        *
        * 高端做法：
        * 1.关注/取关时，关联关系保存在redis中，不依赖数据库；
        * 2.查询数据库后，遍历redis，避免第二次查询数据库
        *
        * */



        Map<String, Object> map = new HashMap<>();
        map.put("myId", myId);
        List<FansVO> fans = fansMapperCustom.queryMyFans(map);

        for(FansVO fan : fans){
            String relation = redis.get(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":" + myId + ":" + fan.getFanId());
            if(StringUtils.isNotBlank(relation) && relation.equalsIgnoreCase("1")) fan.setFriend(true);
        }

        PageHelper.startPage(page, pageSize);
        return setterPagedGrid(fans, page);
    }
}
