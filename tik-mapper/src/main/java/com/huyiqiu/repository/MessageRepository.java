package com.huyiqiu.repository;


import com.huyiqiu.mo.MessageMO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<MessageMO, String> {
    // 通过实现Repository，自定义查询条件
    List<MessageMO> findAllByToUserIdOrderByCreateTimeDesc(String toUserId,
                                                           Pageable pageable);

}
