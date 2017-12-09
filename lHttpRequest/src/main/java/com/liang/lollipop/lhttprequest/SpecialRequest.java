package com.liang.lollipop.lhttprequest;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Lollipop on 2017/9/30.
 * 提交json的请求
 */
public abstract class SpecialRequest {

    protected HttpRequest httpRequest;

    public SpecialRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public abstract SpecialRequest addParameter(String name, Object value);

    public abstract RequestBody getRequestBody();

    public Response execute()throws IOException{
        //获取返回值
        return httpRequest.execute(getRequestBody());
    }

    public void excuteAsyn(HttpRequest.RequestCallBack callBack){
        httpRequest.excuteAsyn(getRequestBody(),callBack);
    }

    public synchronized SpecialRequest addRequestListener(HttpRequest.RequestListener requestListener) {
        httpRequest.addRequestListener(requestListener);
        return this;
    }

    public synchronized SpecialRequest addResponseListener(HttpRequest.ResponseListener responseListener) {
        httpRequest.addResponseListener(responseListener);
        return this;
    }

    public SpecialRequest url(String url){
        httpRequest.url(url);
        return this;
    }

    public SpecialRequest url(HttpUrl url){
        httpRequest.url(url);
        return this;
    }
    public SpecialRequest addHeader(String name, String value){
        httpRequest.addHeader(name,value);
        return this;
    }

}
