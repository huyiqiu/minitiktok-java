package com.huyiqiu.mapper;

import com.huyiqiu.my.mapper.MyMapper;
import com.huyiqiu.pojo.Fans;
import com.huyiqiu.vo.FansVO;
import com.huyiqiu.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FansMapperCustom extends MyMapper<Fans> {
    public List<VlogerVO> queryMyFollows(@Param("paramMap") Map<String, Object> map);


    public List<FansVO> queryMyFans(@Param("paramMap") Map<String, Object> map);
}