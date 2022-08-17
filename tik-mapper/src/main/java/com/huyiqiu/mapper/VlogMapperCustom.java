package com.huyiqiu.mapper;

import com.huyiqiu.vo.IndexVlogVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface VlogMapperCustom{

    public List<IndexVlogVO> getIndexVlogList(@Param("paramMap") Map<String, Object> map);

    public List<IndexVlogVO> getVlogDetailById(@Param("paramMap") Map<String, Object> map);

    public List<IndexVlogVO> getMyLikedVlogList(@Param("paramMap") Map<String, Object> map);

    //关注列表视频
    public List<IndexVlogVO> getMyFollowVlogList(@Param("paramMap") Map<String, Object> map);

    //朋友列表视频
    public List<IndexVlogVO> getMyFriendVlogList(@Param("paramMap") Map<String, Object> map);
}