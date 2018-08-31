package com.tumei.groovy;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Created by leon on 2017/9/2.
 */
@Document(collection = "GroovyScripts")
public class GroovyScriptBean {
    @Id
    public String objectId;
    @Indexed(unique = true, name = "_name")
    public String name;
    public String content = "";
    public Date ts;

    public GroovyScriptBean() {}
    public GroovyScriptBean(String name, String content) {
        this.name = name;
        this.content = content;
        this.ts = new Date();
    }
}
