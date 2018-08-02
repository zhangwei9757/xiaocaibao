package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.model.beans.StarHeroFragsBean;
import com.tumei.modelconf.StarConf;
import com.tumei.common.Readonly;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/17 0017.
 *
 * 占星台
 *
 */
@Document(collection = "Role.Star")
public class StarBean {

	public StarBean() {}

	public StarBean(long _id) {
		id = _id;
	}

	@JsonIgnore
	@Id
	private String ObjectId;
	@Field("id")
	private Long id;

	/**
	 * 当前的星级
	 */
	private List<StarHeroFragsBean> stars = new ArrayList<>();

	public String getObjectId() {
		return ObjectId;
	}

	public void setObjectId(String objectId) {
		ObjectId = objectId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<StarHeroFragsBean> getStars() {
		return stars;
	}

	public void setStars(List<StarHeroFragsBean> stars) {
		this.stars = stars;
	}

	/**
	 * 根据当前副本所在的数量，更新stars是否有新增的
	 * @param scene
	 */
	public void updateStars(int scene) {
		--scene;
		if (scene >= 0) {
			int limit = 13; // 最大条数
			List<StarConf> scs = Readonly.getInstance().findStars();
			for (StarConf sc : scs) {
				if (sc.limit > scene) {
					limit = sc.key - 1;
					break;
				}
			}

			for (int i = stars.size(); i < limit; ++i) {
				StarHeroFragsBean sfb = new StarHeroFragsBean(i+1);
				stars.add(sfb);
			}
		}

		long now = System.currentTimeMillis() / 1000;
		for (StarHeroFragsBean sfb : stars) {
			sfb.check(now);
		}

	}
}
