package com.liang.lollipop.lhttprequest;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by Lollipop on 2017/9/30.
 * 提交json的请求
 */
public class JsonRequest extends SpecialRequest {

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private JSONObject jsonObject = new JSONObject();

    public JsonRequest(HttpRequest httpRequest) {
        super(httpRequest);
    }

    @Override
    public JsonRequest addParameter(String name,Object value){
        name = name==null?"":name;
        value = value==null?"":value;
        try{
            jsonObject.put(name,value);
        }catch (Exception e){
            throw new RuntimeException("JsonRequest.addParameter()",e);
        }
        return this;
    }


    @Override
    public RequestBody getRequestBody(){
        //获取返回值
        String json = jsonObject.toString();
        return RequestBody.create(MEDIA_TYPE_JSON,json);
    }

}
