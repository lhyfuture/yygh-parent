package com.atguigu.yygh.order.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class HttpRequestHelper {
    /**
     *
     * @param paramMap
     * @return
     */
    public static Map<String, Object> switchMap(Map<String, String[]> paramMap) {
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, String[]> param : paramMap.entrySet()) {
            resultMap.put(param.getKey(), param.getValue()[0]);
        }
        return resultMap;
    }

    /**
     * 请求数据获取签名
     * @param paramMap
     * @param signKey
     * @return
     */
//    public static String getSign(Map<String, Object> paramMap, String signKey) {
//        if(paramMap.containsKey("sign")) {
//            paramMap.remove("sign");
//        }
//        TreeMap<String, Object> sorted = new TreeMap<>(paramMap);
//        StringBuilder str = new StringBuilder();
//        for (Map.Entry<String, Object> param : sorted.entrySet()) {
//            str.append(param.getValue()).append("|");
//        }
//        str.append(signKey);
//
//        String md5Str = MD5.encrypt(str.toString());
//
//        return md5Str;
//    }

    /**
     * 签名校验
     * @param paramMap
     * @param signKey
     * @return
     */
//    public static boolean isSignEquals(Map<String, Object> paramMap, String signKey) {
//        String sign = (String)paramMap.get("sign");
//        String md5Str = getSign(paramMap, signKey);
//        if(!sign.equals(md5Str)) {
//            return false;
//        }
//        return true;
//    }

    /**
     * 获取时间戳
     * @return
     */
    public static long getTimestamp() {
        return new Date().getTime();
    }

    /**
     * 封装同步请求
     */
    public static JSONObject sendRequest(Map<String, Object> paramMap, String url){
        String result = "";
        try {
            //封装post参数
            StringBuilder postdata = new StringBuilder();
            for (Map.Entry<String, Object> param : paramMap.entrySet()) {
                postdata.append(param.getKey()).append("=")
                        .append(param.getValue()).append("&");
            }

            byte[] reqData = postdata.toString().getBytes("utf-8");
            byte[] respdata = HttpUtil.doPost(url,reqData);
            result = new String(respdata);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return JSONObject.parseObject(result);
    }
}

