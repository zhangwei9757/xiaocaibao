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
        int zone = uid % 1000
        if (zone <= 5) {
            return 1
        }

        return zone
    }

    @Override
    int chooseArena(long uid) {
        return 1
    }
}
