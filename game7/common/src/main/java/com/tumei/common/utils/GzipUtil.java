package com.tumei.common.utils;

import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Administrator on 2016/11/24 0024.
 */
public class GzipUtil {

    /**
     * 字节流压缩
     * @param data
     *      输入待压缩的字节流
     * @return
     *      返回压缩后的字节流
     */
    public static byte[] Compress(byte[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }

        GZIPOutputStream  gzipOutputStream = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            gzipOutputStream = new GZIPOutputStream(outputStream);
            gzipOutputStream.write(data);
            gzipOutputStream.close();
            outputStream.close();
        } catch (IOException ioe) {
            LogFactory.getLog(GzipUtil.class).error("解压缩失败:", ioe);
            return null;
        } finally {
        }
        return outputStream.toByteArray();
    }

    /**
     * 字节解压缩
     * @param data
     *        待解压缩字节流
     * @return
     *        解压缩后的字节流
     */
    public static byte[] Decompress(byte[] data) {
        if (data == null || data.length <= 0) {
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
            byte[] buffer = new byte[256];
            int n;
            while ((n = gzipInputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, n);
            }
        } catch (IOException ioe) {
            LogFactory.getLog(GzipUtil.class).error("解压缩失败:", ioe);
            return null;
        }
        return outputStream.toByteArray();
    }
}
