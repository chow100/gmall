package com.chow.gmall.manage.handler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ManageHandler {

//    @ResponseBody
    @RequestMapping("index")
    public String test(){

        return "index";
    }

    @RequestMapping("attrListPage")
    public String getArrtList(){

        return "attrListPage";
    }

}
