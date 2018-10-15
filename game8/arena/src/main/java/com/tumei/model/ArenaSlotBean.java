package com.tumei.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Leon on 2017/11/17 0017.
 */
@Data
@Document(collection = "ArenaSlots")
public class ArenaSlotBean {
	@Id
	private String id;

	private int slot;

	private LadderGroup[] groups = new LadderGroup[7];
}
