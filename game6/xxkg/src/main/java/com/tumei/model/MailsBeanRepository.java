package com.tumei.model;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by leon on 2016/11/5.
 *
 * spring jpa的特性：
 * 1. 可以根据findByXXX 方法的签名来提供 查询条件
 * 2. 可以提供@Query注解来查询
 *
 */
public interface MailsBeanRepository extends MongoRepository<MailsBean, Long> {
    List<MailsBean> findAll();
    MailsBean findById(Long id);
}
