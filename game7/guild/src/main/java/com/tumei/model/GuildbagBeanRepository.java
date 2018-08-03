package com.tumei.model;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Created by Administrator on 2018/08/03
 * <p>
 * spring jpa的特性：
 * 1. 可以根据findByXXX 方法的签名来提供 查询条件
 * 2. 可以提供@Query注解来查询
 */
public interface GuildbagBeanRepository extends MongoRepository<GuildbagBean, Long> {
    List<GuildbagBean> findAll();

    GuildbagBean findById(Long id);
}
