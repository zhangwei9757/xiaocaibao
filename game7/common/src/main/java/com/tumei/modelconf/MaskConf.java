package com.tumei.modelconf;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;

/**
 * Created by Administrator on 2017/1/17 0017.
 */
@Document(collection = "Mask")
public class MaskConf {
	@Id
	public String ObjectId;

	public int key;
	public int hero;
	public int quality;
	public int[] basic;
	public int[] stratt;
	public int[][] bonus;
	public int grouplimit;
	public int[] pdd;
}
