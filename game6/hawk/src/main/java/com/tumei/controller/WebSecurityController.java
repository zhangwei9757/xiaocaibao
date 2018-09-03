package com.tumei.controller;

import com.tumei.configs.security.WebSecurityConf;
import com.tumei.configs.security.WebSecurityMongo;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by zw on 2018/08/29
 * <p>
 * 动态配置Security配置
 */
@RestController
@RequestMapping(value = "/HawdSecurity")
public class WebSecurityController {

    @Autowired
    private WebSecurityMongo webSecurityMongo;

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "url", value = "指定路径", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "mode", value = "指定模块", required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "role", value = "指定生产环境角色权限", required = false, dataType = "String", paramType = "query")
    })
    public List<WebSecurityConf> select(String url, String mode, String role) {
        WebSecurityConf wsc = wsc = new WebSecurityConf(url, mode, role);
        return webSecurityMongo.findByfuzzy(wsc);
    }

    @ApiOperation(value = "增加")
    @RequestMapping(value = "/insert", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "url", value = "指定路径", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "mode", value = "指定模块", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "role", value = "指定生产环境角色权限", required = false, dataType = "String", paramType = "query")
    })
    public String insert(String url, String mode, String role) throws Exception {
        if (checkUrl(url)) {
            return "路径参数不合法！";
        }
        WebSecurityConf wsc = new WebSecurityConf(url, mode, role);
        int insert = webSecurityMongo.insert(wsc);
        return insert > 0 ? "成功" : "失败";
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "url", value = "指定路径", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "mode", value = "指定模块", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "role", value = "指定生产环境角色权限", required = false, dataType = "String", paramType = "query")
    })
    public String delete(String url, String mode, String role) throws Exception {
        if (checkUrl(url)) {
            return "路径参数不合法！";
        }
        WebSecurityConf wsc = new WebSecurityConf(url, mode, role);
        int delete = webSecurityMongo.delete(wsc);
        return delete > 0 ? "成功" : "失败";
    }

    @ApiOperation(value = "修改")
    @RequestMapping(value = "/update", method = RequestMethod.GET)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "url", value = "指定路径", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "mode", value = "指定模块", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "role", value = "指定生产环境角色权限", required = false, dataType = "String", paramType = "query")
    })
    public String update(String url, String mode, String role) throws Exception {
        if (checkUrl(url)) {
            return "路径参数不合法！";
        }
        WebSecurityConf wsc = new WebSecurityConf(url, mode, role);
        int update = webSecurityMongo.update(wsc);
        return update > 0 ? "成功" : "失败";
    }

    // 检查路径是不是以“/”开头
    public boolean checkUrl(String url) {
        if (url.equalsIgnoreCase("*")) { //  * 直接返回
            return false;
        }
        if (!StringUtils.startsWithIgnoreCase(url,"/")) {
            // 当参数不合法时，返回true
            return true;
        }
        return false;
    }

    @ApiOperation(value = "test")
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello() {

        return "hello";
    }
}