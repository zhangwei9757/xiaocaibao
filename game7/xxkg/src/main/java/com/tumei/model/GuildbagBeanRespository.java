package com.tumei.model;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GuildbagBeanRespository extends MongoRepository<GuildbagBean, Long> {
    List<GuildbagBean> findAll();

    GuildbagBean findById(Long id);
}
