package com.huyiqiu.mo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document("message")
public class MessageMO {

    @Id
    private String id; // 消息主键Id

    @Field("fromUserId")
    private String fromUserId;
    @Field("fromNickname")// 产生消息的用户名
    private String fromNickname; // ~昵称
    @Field("fromFace")
    private String fromFace; // ~头像
    @Field("toUserId")
    private String toUserId; // 收到消息的用户id

    @Field("msgType")
    private Integer msgType; // 消息类型 枚举

    @Field("msgContent")
    private Map msgContent; // 消息内容

    @Field("createTime")
    private Date createTime; // 创建时间

}
