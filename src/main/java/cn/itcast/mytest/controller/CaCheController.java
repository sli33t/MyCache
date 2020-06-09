package cn.itcast.mytest.controller;

import cn.itcast.mytest.cache.QsCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CaCheController {

    @Autowired
    private QsCache myCache;

    @RequestMapping(value = "/put", method = RequestMethod.GET)
    public String putCache(String key, String value, Long time){
        if (time==null){
            return "time is null, failed";
        }

        if (key==null){
            return "key is null, failed";
        }

        if (value==null){
            return "value is null, failed";
        }

        myCache.putCache(key, value, time);
        return "put success";
    }

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public String getCache(String key){
        Object cache = myCache.getCache(key);
        if (cache==null){
            return "cache is null, failed";
        }
        return cache.toString();
    }

    @RequestMapping(value = "/keys", method = RequestMethod.GET)
    public String getKeys(){
        List<String> keys = myCache.getKeys();
        return keys.toString();
    }

    @RequestMapping(value = "/aof", method = RequestMethod.GET)
    public String aof(Integer open){
        if (open==null){
            return "open is null, failed";
        }

        if (open==1||open==0){
            myCache.aof(open);
            return "aof success";
        }

        return "open not 1&0, failed";
    }

    @RequestMapping(value = "/writeFile", method = RequestMethod.GET)
    public String writeFile(){
        myCache.writeFile();
        return "writeFile success";
    }
}
