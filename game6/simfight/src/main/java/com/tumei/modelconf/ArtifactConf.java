package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Administrator on 2017/1/17 0017.
 * 神器部件
 */
@Document(collection = "Artifact")
public class ArtifactConf {
	@Id
	public String id;

	public int key;

	public int quality;

	public int[] parts; // 激活部件
	public int[] batt; // 基础属性
	public int[] attstr; // 强化属性
	public int[][] satt; // 强化额外获得的属性
	public int[] bateff; // 进场效果
}
