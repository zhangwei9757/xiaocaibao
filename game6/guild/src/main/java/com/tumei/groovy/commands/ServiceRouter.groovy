package com.tumei.groovy.commands

import com.tumei.groovy.contract.IServiceRouter

/**
 * Created by Leon on 2017/11/14 0014.
 */
class ServiceRouter implements IServiceRouter {

    @Override
    int chooseZone(long uid) {
        return uid % 1000
    }

    @Override
    int chooseArena(long uid) {
        return 1
    }
}
