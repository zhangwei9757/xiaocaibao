package com.tumei.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tumei.model.beans.MailBean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/11/5.
 */
@Document(collection = "Role.Mails")
public class MailsBean {
    @JsonIgnore
    @Id
    private String ObjectId;
    @Field("id")
    @Indexed(unique = true, name = "i_id")
    private Long id;
    /**
     * 系统邮件
     */
    private List<MailBean> infos = new ArrayList<>();
    /**
     * 公会邮件
     */
	private List<MailBean> awards = new ArrayList<>();

    public static MailsBean createNewMailsBean(long _id) {
        MailsBean msb = new MailsBean();
        msb.id = _id;
		return msb;
    }

    /**
     * 增加系统邮件
     *
     * @param title
     * @param data
     */
    public void addAwardMail(String title, String data, String _awards) {
		MailBean mb = new MailBean(title, data);
		mb.awards = _awards;

        while (awards.size() > 30) {
            awards.remove(0);
        }
        awards.add(mb);
    }

    /**
     * 增加公会邮件
     *
     * @param title
     * @param data
     */
    public void addInfoMail(String title, String data) {
        MailBean mb = new MailBean(title, data);
		while (infos.size() > 30) {
            infos.remove(0);
        }
        infos.add(mb);
    }

    /**
     * 检查七日过期的邮件,将其删除
     */
    private void check7DayMails() {
        long expires = 24 * 3600 * 7 * 1000;
        infos.removeIf((MailBean mb) -> System.currentTimeMillis() - mb.ts.getTime() >= expires);
        awards.removeIf((MailBean mb) -> System.currentTimeMillis() - mb.ts.getTime() >= expires);
    }

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

    public List<MailBean> getInfos() {
        return infos;
    }

    public void setInfos(List<MailBean> infos) {
        this.infos = infos;
    }

    public List<MailBean> getAwards() {
        return awards;
    }

    public void setAwards(List<MailBean> awards) {
        this.awards = awards;
    }
}
