package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 * 神器部件
 */
@Document(collection = "Artpart")
public class ArtpartConf {
	@Id
	public String id;

	public int key;

	public int quality;

	public int[][] basicatt; // 基础属性
	public int[][] stratt; // 强化属性
	public int[][] advatt; // 附加属性
	public int[] advtag; // 附加属性作用目标
	public int[] reget; // 分解
}
