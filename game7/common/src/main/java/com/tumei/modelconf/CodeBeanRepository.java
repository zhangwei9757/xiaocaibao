package com.tumei.modelconf;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by leon on 2016/11/5.
 *
 * spring jpa的特性：
 * 1. 可以根据findByXXX 方法的签名来提供 查询条件
 * 2. 可以提供@Query注解来查询
 *
 */
public interface CodeBeanRepository extends MongoRepository<CodeBean, String> {
    /**
     * 通过优惠码获取该条信息
     * @param code
     * @return
     */
    CodeBean findById(String code);
}
