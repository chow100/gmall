package com.chow.gmall.list.handler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexHandler {

    @RequestMapping("index")
    public String index(){

        return "index";
    }
}
