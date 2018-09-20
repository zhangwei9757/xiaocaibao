package com.tumei.common.zset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZSet {
    private SkipList<Long,Long> skipList = new SkipList<>(0L);

    private Map<Long, Long> map = new HashMap<>();

    /**
     * 增加分数，对应的具体结构
     *
     * @param score
     * @param member
     */
    public void add(Long score, Long member) {
        Long oldScore = map.get(member);
        if(oldScore != null) {
            if(oldScore.equals(score)) {
                return;
            }

            skipList.delete(score, member);
        }

        skipList.insert(score, member);
        map.put(member, score);
    }

    /**
     * 直接删除指定的人
     *
     * @param member
     */
    public void rem(Long member) {
        Long score = map.get(member);
        if(score != null) {
            skipList.delete(score, member);
            map.remove(member);
        }
    }

    /**
     * 总数量
     *
     * @return
     */
    public int count() {
        return skipList.getCount();
    }

    /**
     * 反向顺序
     *
     * @param rank
     * @return
     */
    private int reverseRank(int rank) {
        return skipList.getCount() - rank + 1;
    }


    public int limit(int count) {
        int total = skipList.getCount();
        if(total <= count) {
            return 0;
        }

        return skipList.deleteByRank(count+1, total, member -> ZSet.this.map.remove(member));
    }

    public int revLimit(int count) {
        int total = skipList.getCount();
        if(total <= count) {
            return 0;
        }

        int from = reverseRank(count+1);
        int to = reverseRank(total);
        return skipList.deleteByRank(from, to, member -> ZSet.this.map.remove(member));
    }

    public List<Long> revRange(int r1, int r2) {
        r1 = reverseRank(r1);
        r2 = reverseRank(r2);
        return range(r1, r2);
    }

    public List<Long> range(int r1, int r2) {
        r1 = Math.max(r1, 1);
        r2 = Math.max(r2, 1);
        return skipList.getRankRange(r1, r2);
    }

    public int revRank(Long member) {
        int rank = rank(member);
        if(rank > 0) {
            return reverseRank(rank);
        }

        return -1;
    }

    /**
     *
     * 查找内容对应的名次
     *
     * @param member
     * @return
     */
    public int rank(Long member) {
        Long score = map.get(member);
        if(score == null) {
            return -1;
        }

        return skipList.getRank(score, member);
    }

    /**
     *
     * 在分数段中查找所有对应的内容
     *
     *
     * @param s1
     * @param s2
     * @return
     */
    public List<Long> rangeByScore(Long s1, Long s2) {
        return skipList.getScoreRange(s1, s2);
    }

    /**
     *
     * 根据内容获取得分
     *
     * @param member
     * @return
     */
    public Long score(Long member) {
        return map.get(member);
    }

    @Override
    public String toString() {
        return skipList.toString();
    }
}
