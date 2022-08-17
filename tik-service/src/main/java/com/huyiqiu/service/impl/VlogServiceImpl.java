package com.huyiqiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.huyiqiu.base.BaseInfoProperties;
import com.huyiqiu.bo.VlogBO;
import com.huyiqiu.enums.MessageEnum;
import com.huyiqiu.enums.YesOrNo;
import com.huyiqiu.mapper.MyLikedVlogMapper;
import com.huyiqiu.mapper.VlogMapper;
import com.huyiqiu.mapper.VlogMapperCustom;
import com.huyiqiu.pojo.MyLikedVlog;
import com.huyiqiu.pojo.Vlog;
import com.huyiqiu.service.FansService;
import com.huyiqiu.service.MsgService;
import com.huyiqiu.service.VlogService;
import com.huyiqiu.utils.PagedGridResult;
import com.huyiqiu.vo.IndexVlogVO;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class VlogServiceImpl extends BaseInfoProperties implements VlogService {

    @Autowired
    private VlogMapper vlogMapper;

    @Autowired
    private VlogMapperCustom vlogMapperCustom;

    @Autowired
    private FansService fansService;

    @Autowired
    private Sid sid;

    @Autowired
    private MyLikedVlogMapper myLikedVlogMapper;

    @Autowired
    private MsgService msgService;

    @Transactional
    @Override
    public void createVlog(VlogBO vlogBO) {
        // if(vlogBO == null) System.out.println("service.. there is no vlogBO..");
        String vid = sid.nextShort();

        Vlog vlog = new Vlog();
        BeanUtils.copyProperties(vlogBO, vlog);

        vlog.setId(vid);

        vlog.setLikeCounts(0);
        vlog.setCommentsCounts(0);
        vlog.setIsPrivate(YesOrNo.NO.type);

        vlog.setCreatedTime(new Date());
        vlog.setUpdatedTime(new Date());

        vlogMapper.insert(vlog);
    }

    // 主页（推荐）的视频列表
    @Override
    public PagedGridResult getIndexVlogList(String search, String userId, Integer page, Integer pageSize) {

        // 切面， 在拿到数据后自动limit分页
        PageHelper.startPage(page, pageSize);

        Map<String, Object> map = new HashMap<>();
        if(StringUtils.isNotBlank(search))
            map.put("search", search);
        List<IndexVlogVO> list = vlogMapperCustom.getIndexVlogList(map);

        for(IndexVlogVO vlog : list){
            if(StringUtils.isNotBlank(userId)){
                //判断我是否关注该博主
                vlog.setDoIFollowVloger(fansService.queryIfFollow(userId, vlog.getVlogerId()));
                //判断是否点赞过
                vlog.setDoILikeThisVlog(doILikeVlog(userId, vlog.getVlogId()));
            }
            //获得当前视频被点赞过的总数
            vlog.setLikeCounts(getVlogBeLikedCounts(vlog.getVlogId()));
        }

        return setterPagedGrid(list, page); // 把list封装成带有自定义分页的grid
    }

    private boolean doILikeVlog(String userId, String vlogId){
        return redis.get(REDIS_USER_LIKE_VLOG + ":" + userId + ":" + vlogId) != null;
    }

    @Override
    public Integer getVlogBeLikedCounts(String vlogId){
        return Integer.valueOf(redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId) == null ? "0" : redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + vlogId));
    }

    @Override
    public IndexVlogVO getVlogDetailById(String userId, String vlogId) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("vlogId", vlogId);
        List<IndexVlogVO> list = vlogMapperCustom.getVlogDetailById(map);
        if(list != null && list.size() != 0){
            IndexVlogVO vlog = list.get(0);
            // VO中默认不关注博主，在这里需要设置成关注
            vlog.setDoIFollowVloger(true);
            // 判断用户是否点赞视频
            vlog.setDoILikeThisVlog(doILikeVlog(userId, vlog.getVlogId()));
            //获得当前视频被点赞过的总数
            vlog.setLikeCounts(getVlogBeLikedCounts(vlog.getVlogId()));
            return vlog;
        }

        return null;
    }

    @Transactional
    @Override
    public void changeToPrivateOrPublic(String vlogId, String userId, Integer yesOrNo) {

        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("id", vlogId);

        Vlog pendingVlog = new Vlog();
        pendingVlog.setIsPrivate(yesOrNo);

        vlogMapper.updateByExampleSelective(pendingVlog, example);

        // return;
    }

    @Override
    public PagedGridResult queryMyVlogList(String userId, Integer page, Integer pageSize, Integer yesOrNo) {

        Example example = new Example(Vlog.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("vlogerId", userId);
        criteria.andEqualTo("isPrivate", yesOrNo);

        PageHelper.startPage(page, pageSize);

        List<Vlog> vlogs = vlogMapper.selectByExample(example);


        return setterPagedGrid(vlogs, page);
    }

    @Transactional
    @Override
    public PagedGridResult queryMyLikedVlogList(String userId, Integer page, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("userId", userId);

        PageHelper.startPage(page, pageSize);
        List<IndexVlogVO> list = vlogMapperCustom.getMyLikedVlogList(map);

        return setterPagedGrid(list, page);
    }

    // 关注页 我关注的博主的视频
    @Transactional
    @Override
    public PagedGridResult getMyFollowVlogList(String userId, Integer page, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("myId", userId);

        List<IndexVlogVO> list = vlogMapperCustom.getMyFollowVlogList(map);
        //对于我关注的博主的视频同样需要遍历判断，计算
        for(IndexVlogVO vlog : list){

            if(StringUtils.isNotBlank(userId)){
                // VO中默认不关注博主，在这里需要设置成关注
                vlog.setDoIFollowVloger(true);
                // 判断用户是否点赞视频
                vlog.setDoILikeThisVlog(doILikeVlog(userId, vlog.getVlogId()));
            }
            //获得当前视频被点赞过的总数
            vlog.setLikeCounts(getVlogBeLikedCounts(vlog.getVlogId()));
        }

        PageHelper.startPage(page, pageSize);
        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public PagedGridResult getMyFriendVlogList(String userId, Integer page, Integer pageSize) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("myId", userId);

        PageHelper.startPage(page, pageSize);
        List<IndexVlogVO> list = vlogMapperCustom.getMyFriendVlogList(map);
        //对于我关注的博主的视频同样需要遍历判断，计算
        for(IndexVlogVO vlog : list){

            if(StringUtils.isNotBlank(userId)){
                // 设置关注
                vlog.setDoIFollowVloger(true);
                // 判断用户是否点赞视频
                vlog.setDoILikeThisVlog(doILikeVlog(userId, vlog.getVlogId()));
            }
            //获得当前视频被点赞过的总数
            vlog.setLikeCounts(getVlogBeLikedCounts(vlog.getVlogId()));
        }

        return setterPagedGrid(list, page);
    }


    @Transactional
    @Override
    public void userLikeVlog(String userId, String vlogId) {
        String rid = sid.nextShort();
        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setId(rid);
        myLikedVlog.setVlogId(vlogId);
        myLikedVlog.setUserId(userId);
        myLikedVlogMapper.insert(myLikedVlog);

        // 系统消息，点赞短视频, 通过vlogId获取用户id
        // 非重要信息，如果操作失败会回滚，需要用mq解耦
        Vlog vlog = getVlog(vlogId);
        String vloger = vlog.getVlogerId();

        Map msgContent = new HashMap<>();
        msgContent.put("vlogId", vlogId);
        msgContent.put("vlogCover", vlog.getCover());
        msgService.createMsg(userId, vloger, MessageEnum.LIKE_VLOG.type, msgContent);

    }

    @Transactional
    @Override
    public void refreshLike(String vlogId, Integer counts) {
        Vlog vlog = new Vlog();
        vlog.setId(vlogId);
        vlog.setLikeCounts(counts);
        vlogMapper.updateByPrimaryKeySelective(vlog);
    }

    @Override
    public Vlog getVlog(String vlogId){
        return vlogMapper.selectByPrimaryKey(vlogId);
    }

    @Transactional
    @Override
    public void userUnlikeVlog(String userId, String vlogId) {

        MyLikedVlog myLikedVlog = new MyLikedVlog();
        myLikedVlog.setVlogId(vlogId);
        myLikedVlog.setUserId(userId);
        myLikedVlogMapper.delete(myLikedVlog);



    }
}
