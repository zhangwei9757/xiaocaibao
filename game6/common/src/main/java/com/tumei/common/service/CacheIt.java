package com.tumei.common.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2016/11/29 0029.
 */
@Component
public class CacheIt {
    private Log log = LogFactory.getLog(CacheIt.class);
    private ScheduledExecutorService ses;
    private List<LoadingCache> caches;

    /***
     * 初始化缓存管理器
     */
    @PostConstruct
    public void initialize() {
        log.warn(this.getClass().getName() + " init.");
        caches = new ArrayList<LoadingCache>();
    }

    // 一个小时佐佑 进行一次强制的全体刷新
//    @Scheduled(fixedDelay = 3600000)
    void clean() {
//		log.debug("强制缓存清理，线程(" + Thread.currentThread().getId() + ").");
		cleanUp(false);
    }

    /**
     * 关闭定时器
     */
    @PreDestroy
    public void dispose() {
        log.warn("--- dispose: " + this.getClass().getName());
        // 手动超时
        cleanUp(true);
    }

    /**
     *
     * 清理管理的所有LoadingCache,一般用于程序整体退出，或者预定时间点
     *
     */
    public void cleanUp(boolean force) {
        for (LoadingCache cache : caches) {
            if (force) {
                cache.invalidateAll();
            } else {
                cache.cleanUp();
            }
        }
    }

    /**
     * 强制刷新某一个玩家
     * @param uid
     */
    public void invalidate(Long uid) {
        for (LoadingCache cache : caches) {
        	cache.invalidate(uid);
        }

        caches.get(0).refresh(uid);
    }

    /***
     * 便捷方法返回缓存数据集
     *
     * @param cacheLoader  读取的时候如何从数据库拉取
     * @param removeListener 失效的时候如何将数据返填到数据库
     * @param <K>  key 类型
     * @param <V>  value 类型
     * @return
     *          返回一个这样的缓存数据集合
     */
    public <K, V> LoadingCache<K, V> cached(CacheLoader<K, V> cacheLoader, RemovalListener<K, V> removeListener, int accDelay, int writeDelay) {
        LoadingCache<K, V> cache = CacheBuilder.newBuilder().expireAfterAccess(accDelay, TimeUnit.SECONDS).expireAfterWrite(writeDelay, TimeUnit.SECONDS).removalListener(removeListener).build(cacheLoader);
        caches.add(cache);
        return cache;
    }
}
