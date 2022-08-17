package com.huyiqiu.controller;


import com.huyiqiu.MinIOConfig;
import com.huyiqiu.base.BaseInfoProperties;
import com.huyiqiu.bo.UpdateUserBO;
import com.huyiqiu.enums.FileTypeEnum;
import com.huyiqiu.enums.UserInfoModifyType;
import com.huyiqiu.grace.result.GraceJSONResult;
import com.huyiqiu.grace.result.ResponseStatusEnum;
import com.huyiqiu.pojo.Users;
import com.huyiqiu.service.UserService;
import com.huyiqiu.utils.MinIOUtils;
import com.huyiqiu.vo.UsersVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Api(tags = "UserInfoController 用户信息接口")
@RequestMapping("userInfo")
@RestController
public class UserInfoController extends BaseInfoProperties {

    @Autowired
    UserService userService;

    @GetMapping("query")
    public GraceJSONResult query(@RequestParam String userId) throws Exception {
        Users user = userService.getUser(userId);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);

        // 我的关注总数
        String myFollowsCountsStr = redis.get(REDIS_MY_FOLLOWS_COUNTS + ":" + userId);
        // 我的粉丝总数
        String myFansCountsStr = redis.get(REDIS_MY_FANS_COUNTS + ":" + userId);
        // 用户获赞总数
        // String LikedVlogCountsStr = redis.get(REDIS_VLOG_BE_LIKED_COUNTS + ":" + userId);
        String LikedVlogerCountsStr = redis.get(REDIS_VLOGER_BE_LIKED_COUNTS + ":" + userId);

        Integer myFollowsCounts = 0;
        Integer myFansCounts = 0;
        Integer likeVlogerCounts = 0;
        // Integer likeVlogCounts = 0;
        // Integer totalLikeMeCounts = 0;

        if(StringUtils.isNotBlank(myFollowsCountsStr)) myFollowsCounts = Integer.valueOf(myFollowsCountsStr);
        if(StringUtils.isNotBlank(myFansCountsStr)) myFansCounts = Integer.valueOf(myFansCountsStr);
        // if(StringUtils.isNotBlank(LikedVlogCountsStr)) likeVlogCounts = Integer.valueOf(LikedVlogCountsStr);
        if(StringUtils.isNotBlank(LikedVlogerCountsStr)) likeVlogerCounts = Integer.valueOf(LikedVlogerCountsStr);
        // totalLikeMeCounts = likeVlogCounts + likeVlogerCounts;

        usersVO.setMyFollowsCounts(myFollowsCounts);
        usersVO.setMyFansCounts(myFansCounts);
        usersVO.setTotalLikeMeCounts(likeVlogerCounts);

        return GraceJSONResult.ok(usersVO);
    }

    @PostMapping("modifyUserInfo")
    public GraceJSONResult modifyUserInfo(@RequestBody UpdateUserBO updateUserBO,
                                          @RequestParam Integer type)
            throws Exception{

        UserInfoModifyType.checkUserInfoTypeIsRight(type);


        Users newUserInfo = userService.updateUserInfo(updateUserBO, type);


        return GraceJSONResult.ok(newUserInfo);
    }

    @Autowired
    private MinIOConfig minIOConfig;

    @PostMapping("modifyImage")
    public GraceJSONResult modifyImage(@RequestParam String userId,
                                       @RequestParam Integer type,
                                       MultipartFile file) throws Exception{

        if(type != FileTypeEnum.BGIMG.type && type != FileTypeEnum.FACE.type)
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);

        String filename = file.getOriginalFilename();
        MinIOUtils.uploadFile(minIOConfig.getBucketName(), filename, file.getInputStream());

        String imgUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + filename;

        //在数据库中修改用户头像的地址
        UpdateUserBO updateUserBO = new UpdateUserBO();
        updateUserBO.setId(userId);
        if(type == FileTypeEnum.FACE.type) updateUserBO.setFace(imgUrl);
        if(type == FileTypeEnum.BGIMG.type) updateUserBO.setBgImg(imgUrl);

        Users user = userService.updateUserInfo(updateUserBO);

        return GraceJSONResult.ok(user); // 前端需要一个user
    }
}
