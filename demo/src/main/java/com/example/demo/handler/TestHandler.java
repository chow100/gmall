package com.example.demo.handler;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestHandler {

    @RequestMapping("index")
    @ResponseBody
    public String test(){
        return "aaabbb";
    }
}
