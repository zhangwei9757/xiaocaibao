package com.tumei.model.limit;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by zw on 2018/09/12
 *
 * spring jpa的特性：
 * 1. 可以根据findByXXX 方法的签名来提供 查询条件
 * 2. 可以提供@Query注解来查询
 *
 */
public interface InvadingBeanRepository extends MongoRepository<InvadingBean, Long> {
    List<InvadingBean> findAll();
    InvadingBean findById(Long id);
}