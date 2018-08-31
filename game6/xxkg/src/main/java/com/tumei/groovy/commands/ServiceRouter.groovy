package com.tumei.groovy.commands

import com.tumei.groovy.contract.IServiceRouter
import org.springframework.stereotype.Component

/**
 * Created by Leon on 2017/11/14 0014.
 */
@Component
class ServiceRouter implements IServiceRouter{

    @Override
    int chooseZone(long uid) {
        return uid % 1000
    }

    @Override
    int chooseArena(long uid) {
        int zone = chooseZone(uid) - 1

        return (int)(zone / 20) + 1
    }
}
