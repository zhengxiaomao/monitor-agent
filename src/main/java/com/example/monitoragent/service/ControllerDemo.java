package com.example.monitoragent.service;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@ResponseBody
@RequestMapping("/v1")
public class ControllerDemo {


    @RequestMapping(value = "/haha",method = RequestMethod.GET)
    public Map result(String name,int age,String sex){

        Map map=new HashMap();
        map.put("name",name);
        map.put("age",age);
        map.put("sex",sex);
        return map;
    }
}
