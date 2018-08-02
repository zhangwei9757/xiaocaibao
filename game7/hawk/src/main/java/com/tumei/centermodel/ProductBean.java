package com.tumei.centermodel;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by leon on 2016/11/5.
 *
 * 游戏中的商品
 */
@Document(collection = "Products")
public class ProductBean {
	@Id
    public String objectId;

	@Indexed(unique = true)
    public String product;
    public int rmb;

    @Override
    public String toString() {
        return "ProductBean{" +
                "rmb=" + rmb +
                ", product='" + product + '\'' +
                '}';
    }
}
