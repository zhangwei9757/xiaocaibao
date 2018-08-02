package com.tumei.game.protos.group;

import com.tumei.websocket.BaseProtocol;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leon on 2016/12/31.
 */
@Component
public class GroupTextNotify extends BaseProtocol {
	public String[] text;
}
