package com.huyiqiu.service.impl;

import com.huyiqiu.base.BaseInfoProperties;
import com.huyiqiu.enums.MessageEnum;
import com.huyiqiu.mo.MessageMO;
import com.huyiqiu.pojo.Users;
import com.huyiqiu.repository.MessageRepository;
import com.huyiqiu.service.FansService;
import com.huyiqiu.service.MsgService;
import com.huyiqiu.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MsgServiceImpl extends BaseInfoProperties implements MsgService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private FansService fansService;

    @Override
    public void createMsg(String fromUserId, String toUserId, Integer type, Map msgContent) {

        Users userFrom = userService.getUser(fromUserId);
        MessageMO messageMO = new MessageMO();
        messageMO.setFromUserId(userFrom.getId());
        messageMO.setFromFace(userFrom.getFace());
        messageMO.setFromNickname(userFrom.getNickname());

        messageMO.setToUserId(toUserId);

        messageMO.setMsgType(type);
        if (msgContent != null)
            messageMO.setMsgContent(msgContent);

        messageMO.setCreateTime(new Date());

        messageRepository.save(messageMO);
    }

    @Override
    public List<MessageMO> queryList(String toUserId, Integer page, Integer pageSize) {
        Pageable pageable = PageRequest.of(page,
                                           pageSize,
                                           Sort.Direction.DESC,
                                "createTime");

        List<MessageMO> list = messageRepository.findAllByToUserIdOrderByCreateTimeDesc(toUserId, pageable);

        for(MessageMO msg : list){
            // 如果是关注类型消息，判断是否互粉
            if(msg.getMsgType() == MessageEnum.FOLLOW_YOU.type){
                Map map = new HashMap<>();
                String relationship = redis.get(REDIS_FANS_AND_VLOGER_RELATIONSHIP + ":" + msg.getToUserId() + ":" + msg.getFromUserId());
                if(StringUtils.isNotBlank(relationship) && relationship.equalsIgnoreCase("1")){
                    map.put("isFriend", true);
                } else{
                    map.put("isFriend", false);
                }
                msg.setMsgContent(map);

            }
            String fromUserId = msg.getFromUserId();
            boolean ifFollow = fansService.queryIfFollow(toUserId, fromUserId);

        }

        return list;
    }
}
