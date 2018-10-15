package com.tumei.groovy.contract;

import com.tumei.game.GameUser;
import com.tumei.game.protos.mine.*;
import com.tumei.game.services.mine.MapData;

/**
 * Created by Leon on 2017/9/1 0001.
 */
public interface IMineSystem {

	void update();

	MapData getMapData();

	void leave(long id);

	String dumpPlayers();

	void save();

	/**
	 * @param user
	 * @param proto
	 */
	void enter(GameUser user, RequestMineEnter proto);

	void leave(GameUser user, RequestMineLeave proto);

	void move(GameUser user, RequestMineMove proto);

	void look(GameUser user, RequestMineLook proto);

	void action(GameUser user, RequestMineAction proto);

	void harvest(GameUser user, RequestMineHarvest proto);

	void enhance(GameUser user, RequestMineEnhance proto);

	void buyEnergy(GameUser user, RequestMineBuyEnergy proto);

	void accelerate(GameUser user, RequestMineAccelerate proto);
}
