package com.tumei.common.utils;

import java.util.Base64;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/11/24 0024.
 */
public class RandomUtil {

    private static Random random = new Random(System.currentTimeMillis());

    private static final String CHAR_LIST =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";

    /**
     * 随机数据
     * @return
     */
    public static int getRandom() {
        return Math.abs(random.nextInt());
    }

    public static float getFloat() { return Math.abs(random.nextFloat()); }

    public static int getBetween(int min, int max) {
		if (min == max) {
            return min;
        }
        return (getRandom() % (Math.abs(max - min) + 1)) + min;
    }


    /**
     * 从列表中随机一个
     * @param ls
     * @param <T>
     * @return
     */
    public static <T> T getInList(List<T> ls) {
		int idx = getRandom() % ls.size();
        return ls.get(idx);
    }

    /**
     * 从列表中随机一组，两两一组
     * @param ls
     * @param <T>
     * @return
     */
    public static int getIntArrayDouble(int[] ls) {
        int idx = getRandom() % (ls.length / 2);
		return idx * 2;
    }


    public static <T> T getInArray(T[] ls) {
        int idx = getRandom() % ls.length;
        return ls[idx];
    }

    public static int getInArray(int[] ls) {
        int idx = getRandom() % ls.length;
        return ls[idx];
    }

    public static String randomChars(int count) {
        if (count <= 0) {
            return "";
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; ++i) {
            int r = random.nextInt(CHAR_LIST.length());
            sb.append(CHAR_LIST.charAt(r));
        }
        return sb.toString();
    }

    /**
     * 获取16bit随机数
     * @return
     */
    public static String randomDigest() {
        byte[] bytes = new byte[16];
        long prefix = random.nextLong();
        for (int i = 0; i < 8; ++i) {
            bytes[i] = (byte) (prefix >> (7 - i << 3));
        }

        long suffix = random.nextLong();
        for (int i = 0; i < 8; ++i) {
            bytes[i + 8] = (byte) (suffix >> (7 - i << 3));
        }

        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * weights 是 [id, 权重, id, 权重 ...] 的格式
     * @param weights
     * @return
     *
     * 返回权重对应的id
     */
    public static int randomByWeight(int[] weights) {
        int total = 0;
        int r = getRandom() % 100;
		for (int i = 0; i < weights.length / 2; ++i) {
			total += weights[i*2+1];
			if (r <= total) {
                return weights[i*2];
            }
        }
        return 0;
    }

    /**
     * weights 是 [权重，权重，权重]的格式
     *
     * @param weights
     * @return
     *
     * 返回对应权重的索引[0, ...]
     *
     */
    public static int randomWeightIndex(int[] weights) {
		int total = 0;
        int r = getRandom() % 100;
        for (int i = 0; i < weights.length; ++i) {
            total += weights[i];
            if (r <= total) {
                return i;
            }
        }
        return 0;
    }
}
