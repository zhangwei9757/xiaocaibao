package com.tumei.groovy.commands.controller

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

/**
 * 测试用groovy 脚本
 */
class AdminController {
    private static final Log log = LogFactory.getLog(this.class)

    /* 以下为admin入口 */

    @RequestMapping(value = "/logon_admin", method = RequestMethod.GET)
    String logon_admin(Model model) {
        return "logon";
    }

}



