package com.liang.lollipop.lhttprequest;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.InetAddress;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSink;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Created by Lollipop on 2017/04/20.
 * 创建一个Http请求
 */
public class HttpRequest{

    private final static OkHttpClient okHttpClient;

    private static boolean isInit = false;

    private static CheckNetworkCallback checkNetworkCallback;
    //本次请求的DomainInfo，保存使用的对象，
    //用于上传服务器请求状态，以作为下次IP筛选的依据
//    private DomainInfo domainInfo;

    private MultipartBody.Builder builder;
    private Request.Builder requestBuilder;
    private static ArrayList<RequestListener> requestListeners = null;
    private static ArrayList<ResponseListener> responseListeners = null;

    private MediaType mediaType = MultipartBody.FORM;

    private boolean isGet = false;

    private final static String KEY = "";

    private static ResponseListener thisResponseListener = new ResponseListener() {
        @Override
        public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
            if(responseListeners!=null&&responseListeners.size()>0){
                for(ResponseListener responseListener:responseListeners){
                    if(responseListener!=null) {
                        responseListener.onResponseProgress(bytesRead, contentLength, done);
                    }
                }
            }
        }

        @Override
        public boolean onError(Exception e) {
            if(responseListeners!=null&&responseListeners.size()>0){
                for(ResponseListener responseListener:responseListeners){
                    if(responseListener!=null) {
                        if (responseListener.onError(e)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    };

    static {
        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
//        okBuilder.dns(new DNSPod());
        //添加数据返回下行的进度监听
        okBuilder.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                //拦截
                Response originalResponse = chain.proceed(chain.request());
                //包装响应体并返回
                return originalResponse.newBuilder()
                        .body(new ProgressResponseBody(originalResponse.body(), thisResponseListener))
                        .build();
            }
        });
        okHttpClient = okBuilder.build();
    }

    private static class DNSPod implements Dns {

        private OkHttpClient httpDnsclient;

        @Override
        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            //防御代码
            if (TextUtils.isEmpty(hostname)) {
                throw new UnknownHostException("hostname == null");
            }
            //dnspod提供的dns服务
            HttpUrl httpUrl = new HttpUrl.Builder().scheme("http")
                    .host("119.29.29.29")
                    .addPathSegment("d")
                    .addQueryParameter("dn", hostname)
                    .build();
            Request dnsRequest = new Request.Builder().url(httpUrl).get().build();
            try {
                String s = getHTTPDnsClient().newCall(dnsRequest).execute().body().string();
                //避免服务器挂了却无法查询DNS
                if (!s.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
                    return Dns.SYSTEM.lookup(hostname);
                }
                return Arrays.asList(InetAddress.getAllByName(s));
            } catch (IOException e) {
                return Dns.SYSTEM.lookup(hostname);
            }
        }

        public synchronized OkHttpClient getHTTPDnsClient() {
            if (httpDnsclient == null) {
//                final File cacheDir = GlobalContext.getInstance().getExternalCacheDir();
                httpDnsclient = new OkHttpClient.Builder()
                        //消费者工作线程池
//                        .dispatcher(getDispatcher())
                        //Logger拦截器
//                        .addNetworkInterceptor(new Interceptor() {
//                            @Override public Response intercept(Chain chain) throws IOException {
//                                Response originalResponse = chain.proceed(chain.request());
//                                return originalResponse.newBuilder()
//                                        //在返回header中加入缓存消息
//                                        //下次将不再发送请求
//                                        .header("Cache-Control", "max-age=600").build();
//                            }
//                        })
                        //5MB的文件缓存
//                        .cache(new Cache(new File(cacheDir, "httpdns"), 5 * 1024 * 1024))
                        .build();
            }
            return httpDnsclient;
        }

    }

    public static String getCodeMsg(int code){
        switch (code){
            case 100:
                return "请继续提出请求";
            case 101:
                return "服务器已切换服务协议";
            case 200:
            return "访问成功";
            case 201:
                return "新资源已创建";
            case 202:
                return "服务器已收到请求";
            case 203:
                return "返回数据可能来自其他来源";
            case 204:
                return "服务器未返回任何内容";
            case 205:
                return "内容已重置";
            case 206:
                return "服务器已处理部分请求";
            case 300:
                return "服务器收到多种选择请求";
            case 301:
                return "请求的网页已永久移动到新位置";
            case 302:
                return "服务器返回数据临时从其他页面获取";
            case 303:
                return "请指定具体的访问地址";
            case 304:
                return "返回内容未修改，放弃返回数据";
            case 305:
                return "请使用代理方式访问";
            case 307:
                return "数据来自其他网页";
            case 400:
                return "请求语法错误";
            case 401:
                return "您的身份未验证";
            case 403:
                return "服务器拒绝请求";
            case 404:
                return "请求地址不存在";
            case 405:
                return "请求的方法已被禁用";
            case 406:
                return "服务器不接受非法请求";
            case 407:
                return "请提供代理授权";
            case 408:
                return "网络请求超时";
            case 409:
                return "数据返回时发生冲突";
            case 410:
                return "请求资源已被永久删除";
            case 411:
                return "拒绝未知长度的请求";
            case 412:
                return "请求操作未满足前提条件";
            case 413:
                return "请求内容过大";
            case 414:
                return "请求地址过长";
            case 415:
                return "不支持的媒体格式";
            case 416:
                return "请求范围不符合要求";
            case 417:
                return "未满足服务器需求";
            case 428:
                return "需要前提条件";
            case 429:
                return "请求过多";
            case 431:
                return "请求头部字段太长";
            case 500:
                return "服务器内部错误";
            case 501:
                return "尚未实施，无法完成请求";
            case 502:
                return "服务器网关错误";
            case 503:
                return "服务器不可用";
            case 504:
                return "网关请求超时";
            case 505:
                return "HTTP版本不受支持";
            case 511:
                return "需要网络认证，请使用浏览器打开任意网页认证";
            case 0:
                return "访问失败";
            default:
                return code+"";
        }
    }

    public static String getExceptionInfo(Exception e){
        if(e == null) {
            return "";
        }
        if(e instanceof NetworkException){
            return "当前无网络连接，请检查后再试";
        }
        if(e instanceof JSONException ||e instanceof NumberFormatException){
            return "数据解析失败";
        }
        if(e instanceof UnknownHostException){
            return "域名解析失败，请更换网络模式或清理DNS";
        }
        if(e instanceof java.net.SocketTimeoutException){
            return "网络连接超时，请检查网络状况";
        }
        if(e instanceof java.net.ConnectException){
            return "网络连接错误，请检查网络状况";
        }
        if(e instanceof RequestException) {
            return e.getMessage();
        }
        return "Exception:"+e.getMessage();
    }

    public static class NetworkException extends Exception{
        public NetworkException() {
            this("无网络连接");
        }

        public NetworkException(String message) {
            super(message);
        }

        public NetworkException(String message, Throwable cause) {
            super(message, cause);
        }

        public NetworkException(Throwable cause) {
            super(cause);
        }
    }

    public JsonRequest postWithJson(){
        return new JsonRequest(this);
    }

    public static String getError(int code,Exception e){
        return getCodeMsg(code)+(e==null?"":","+HttpRequest.getExceptionInfo(e));
    }

    private RequestListener thisRequestListener = new RequestListener() {
        @Override
        public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
            if(requestListeners!=null&&requestListeners.size()>0){
                for(RequestListener requestListener:requestListeners){
                    if(requestListener!=null) {
                        requestListener.onRequestProgress(bytesWritten, contentLength, done);
                    }
                }
            }
        }

        @Override
        public boolean onError(Exception e) {
            if(requestListeners!=null&&requestListeners.size()>0){
                for(RequestListener requestListener:requestListeners){
                    if(requestListener!=null) {
                        if (requestListener.onError(e)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    };

    public HttpRequest() {
        if(!isInit){
            throw new RuntimeException("HttpRequest mast be Init");
        }
//        requestListeners = new ArrayList<>();
//        responseListeners = new ArrayList<>();
    }

    private MultipartBody.Builder getBuilder(){
        return new MultipartBody.Builder().setType(mediaType);
    }

    private FormBody.Builder getFormBuilder(){
        return new FormBody.Builder();
    }

    public static HttpRequest request(String url){
        return createRequest().url(url);
    }

    public static HttpRequest request(HttpUrl url){
        return createRequest().url(url);
    }

    private static HttpRequest createRequest(){
        return new HttpRequest();
    }

    public HttpRequest url(String url){
        getRequestBuilder().url(url);
        return this;
    }

    public String executeForString()throws IOException{
        if(checkNetworkCallback!=null&&!checkNetworkCallback.isNetworkConnected()){
            return "";
        }
        return execute().body().string();
    }

    public InputStream executeForStream()throws IOException{
        if(checkNetworkCallback!=null&&!checkNetworkCallback.isNetworkConnected()){
            return null;
        }
        return execute().body().byteStream();
    }

    public byte[] executeForBytes()throws IOException{
        if(checkNetworkCallback!=null&&!checkNetworkCallback.isNetworkConnected()){
            return null;
        }
        return execute().body().bytes();
    }

    public Response execute()throws IOException{
        //获取返回值
        return newCall().execute();
    }

    public Response execute(RequestBody requestBody)throws IOException{
        //获取返回值
        return newCall(requestBody).execute();
    }

    public Call newCall(){
        return okHttpClient.newCall(getRequest());
    }

    public Call newCall(RequestBody requestBody){
        return okHttpClient.newCall(getRequest(requestBody));
    }

    public interface CheckNetworkCallback{
        public boolean isNetworkConnected();
    }

    private Request getRequest(){
        if(builder!=null){
            ProgressRequestBody rb = new ProgressRequestBody(builder.build(), thisRequestListener);
            return getRequest(rb);
        }else{
            return getRequest(null);
        }
    }

    private Request getRequest(RequestBody requestBody){
        if(requestBody!=null){
            requestBuilder.post(requestBody);
        }else{
            requestBuilder.get();
        }
        Request request = requestBuilder.build();
        clear();
        return request;
    }

    private void clear(){
        builder = null;
        requestBuilder = null;
    }

    public Request.Builder getRequestBuilder(){
        if(requestBuilder==null) {
            requestBuilder = new Request.Builder();
        }
        return requestBuilder;
    }

    public void enqueue(Callback callback)throws IOException{
        okHttpClient.newCall(getRequest()).enqueue(callback);
    }

    public HttpRequest addHeader(String name,String value){
        getRequestBuilder().addHeader(name,value);
        return this;
    }

    public HttpRequest url(HttpUrl url){
        getRequestBuilder().url(url);
        return this;
    }

    private MultipartBody.Builder getParameterBuilder(){
        if(builder==null) {
            builder = getBuilder();
        }
        return builder;
    }


    public HttpRequest addParameter(String name,String value){
        name = name==null?"":name;
        value = value==null?"":value;
        getParameterBuilder().addFormDataPart(name,value);
        return this;
    }



    public HttpRequest addParameter(String name,File file){
        String fileName = file.getName();
        RequestBody fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
        //根据文件名设置contentType
        getParameterBuilder().addFormDataPart(name,fileName,fileBody);
        return this;
    }

    private String guessMimeType(String path){
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null){
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public void downloadTo(final String fileDir, final String fileName, final DownloadCallBack callBack){
        TaskUtils.get().runAs(new Runnable() {
            @Override
            public void run() {
                if(checkNetworkCallback!=null&&!checkNetworkCallback.isNetworkConnected()){
                    if(callBack!=null) {
                        callBack.onError(0, null, new NetworkException());
                    }
                    return ;
                }
                Response response = null;
                InputStream inputStream = null;
                FileOutputStream fileOutputStream = null;
                try{
                    response = execute();
                    byte[] buf = new byte[2048];
                    int len = 0;
                    long allLength = 0;
                    long downLength = 0;
                    inputStream = response.body().byteStream();
                    File file = new File(fileDir, fileName);
                    File filePath = new File(fileDir);
                    if (!filePath.exists()) {
                        filePath.mkdirs();
                    }
                    // 下载函数
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    fileOutputStream = new FileOutputStream(file);
                    allLength = response.body().contentLength();
                    while ((len = inputStream.read(buf)) != -1){
                        downLength += len;
                        fileOutputStream.write(buf, 0, len);
                        if(callBack!=null){
                            float pro = 0;
                            if(allLength > 0 && downLength > 0) {
                                pro = downLength * 0.1f / allLength;
                            }
                            callBack.onProgressChange(pro,allLength,downLength);
                        }
                    }
                    fileOutputStream.flush();
                    //如果下载文件成功，第一个参数为文件的绝对路径
                    if(callBack!=null) {
                        callBack.onDownLoadSuccess(file.getAbsolutePath());
                    }
                }catch (Exception e){
                    if(callBack!=null){
                        if(response==null) {
                            callBack.onError(0, null, e);
                        } else {
                            callBack.onError(response.code(), response.request(), e);
                        }
                    }
                }finally {
                    try{
                        if(inputStream!=null) {
                            inputStream.close();
                        }
                    }catch (Exception e){}
                    try{
                        if(fileOutputStream!=null) {
                            fileOutputStream.close();
                        }
                    }catch (Exception e){}
                }
            }
        });
    }

    public void excuteAsyn(RequestCallBack callBack){
        TaskUtils.get().runAs(new RequestRunnable(newCall(),callBack));
    }

    public void excuteAsyn(RequestBody requestBody,final RequestCallBack callBack){
        TaskUtils.get().runAs(new RequestRunnable(newCall(requestBody),callBack));
    }

    private class RequestRunnable implements Runnable{

        private RequestCallBack callBack;
        private Call call;

        RequestRunnable(Call call,RequestCallBack callBack) {
            this.callBack = callBack;
            this.call = call;
        }

        @Override
        public void run() {
            if(checkNetworkCallback!=null&&!checkNetworkCallback.isNetworkConnected()){
                if(callBack!=null) {
                    callBack.error(0, null, null, new NetworkException());
                }
                return;
            }
            Response response = null;
            try{
                response = call.execute();
                if(callBack!=null){
                    if(response.code()==200) {
                        callBack.success(callBack.processing(response));
                    } else {
                        callBack.error(response.code(), response.request(), response, null);
                    }
                }
            }catch (Exception e){
                if(callBack!=null){
                    if(response!=null) {
                        callBack.error(response.code(), response.request(), response, e);
                    } else {
                        callBack.error(0, null, null, e);
                    }
                }
            }
        }
    }

    /**
     * 数据获取回调接口
     * @author LiuJ
     */
    public interface RequestCallBack<T>{
        void success(T object);
        void error(int code, Request request, Response response, Exception e);
        T processing(Response response)throws Exception;
    }

    public static abstract class RequestOnHandlerCallBack<T> implements RequestCallBack<T>{
        protected Handler handler;

        public RequestOnHandlerCallBack(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void success(final T object) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onUISuccess(object);
                }
            });
        }

        @Override
        public void error(final int code, final Request request, final Response response, final Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onUIError(code,request,response,e);
                }
            });
        }

        @Override
        public T processing(Response response)throws Exception {
            return onBackground(response);
        }

        public abstract void onUISuccess(T object);

        public abstract void onUIError(int code,Request request,Response response,Exception e);

        public abstract T onBackground(Response response) throws Exception;

    }

    public static abstract class RequestStringOnHandlerCallBack<T> extends RequestOnHandlerCallBack<T>{

        public RequestStringOnHandlerCallBack(Handler handler) {
            super(handler);
        }

        @Override
        public T onBackground(Response response)throws Exception{
            String res;
            if(response.isSuccessful()){
                res = response.body().string();
                    return onBackground(res);
            }else {
                error(response.code(), response.request(), response, null);
            }
            return null;
        }

        protected abstract T onBackground(String response)throws Exception;

    }

    /**
     * 运行于UI线程的数据获取回掉接口
     * @param <T>
     */
    public static abstract class RequestOnUICallBack<T> implements RequestCallBack<T>{
        protected Activity activity;

        public RequestOnUICallBack(Fragment fragment) {
            this.activity = fragment.getActivity();
        }

        public RequestOnUICallBack(android.app.Fragment fragment) {
            this.activity = fragment.getActivity();
        }

        public RequestOnUICallBack(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void success(final T object) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onUISuccess(object);
                }
            });
        }

        @Override
        public void error(final int code, final Request request, final Response response, final Exception e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onUIError(code,request,response,e);
                }
            });
        }

        @Override
        public T processing(Response response)throws Exception {
            return onBackground(response);
        }

        public abstract void onUISuccess(T object);

        public abstract void onUIError(int code,Request request,Response response,Exception e);

        public abstract T onBackground(Response response) throws Exception;

    }

    public static abstract class RequestStringOnUICallBack<T> extends RequestOnUICallBack<T>{

        public RequestStringOnUICallBack(Activity activity) {
            super(activity);
        }

        public RequestStringOnUICallBack(Fragment fragment) {
            super(fragment);
        }

        public RequestStringOnUICallBack(android.app.Fragment fragment) {
            super(fragment);
        }

        @Override
        public T onBackground(Response response)throws Exception{
            String res = "";
            if(response.isSuccessful()){
                res = response.body().string();
                return onBackground(res);
            }else {
                error(response.code(), response.request(), response, null);
            }
            return null;
        }

        protected abstract T onBackground(String response)throws Exception;

    }

    private static String getFileName(String path){
        int separatorIndex = path.lastIndexOf("/");
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }

    public interface DownloadCallBack{
        void onError(int code, Request request, Exception e);
        void onProgressChange(float pro, long allLength, long downLength);
        void onDownLoadSuccess(String path);
    }

    public static abstract class DownloadOnUICallBack implements DownloadCallBack{

        protected Activity content;

        public DownloadOnUICallBack(Activity content) {
            this.content = content;
        }

        @Override
        public void onError(final int code, final Request request, final Exception e) {
            if(content!=null) {
                content.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onUIError(code, request, e);
                    }
                });
            }
        }

        @Override
        public void onProgressChange(final float pro, final long allLength, final long downLength) {
            if(content!=null) {
                content.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onUIProgressChange(pro, allLength, downLength);
                    }
                });
            }
        }

        @Override
        public void onDownLoadSuccess(final String path) {
            if(content!=null) {
                content.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onUIDownLoadSuccess(path);
                    }
                });
            }
        }

        public abstract void onUIError(int code, Request request, Exception e);

        public abstract void onUIProgressChange(float pro, long allLength, long downLength);

        public abstract void onUIDownLoadSuccess(String path);

    }

    public static abstract class DownloadOnHandlerCallBack implements DownloadCallBack{

        protected Handler handler;

        public DownloadOnHandlerCallBack(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void onError(final int code, final Request request, final Exception e) {
            if(handler!=null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onUIError(code, request, e);
                    }
                });
            }
        }

        @Override
        public void onProgressChange(final float pro, final long allLength, final long downLength) {
            if(handler!=null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onUIProgressChange(pro, allLength, downLength);
                    }
                });
            }
        }

        @Override
        public void onDownLoadSuccess(final String path) {
            if(handler!=null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onUIDownLoadSuccess(path);
                    }
                });
            }
        }

        public abstract void onUIError(int code, Request request, Exception e);

        public abstract void onUIProgressChange(float pro, long allLength, long downLength);

        public abstract void onUIDownLoadSuccess(String path);

    }

    //包装的响体，处理进度
    private static class ProgressResponseBody extends ResponseBody {

        //实际的待包装响应体
        private final ResponseBody responseBody;
        //进度回调接口
        private final ResponseListener progressListener;
        //包装完成的BufferedSource
        private BufferedSource bufferedSource;

        /**
         * 构造函数，赋值
         * @param responseBody 待包装的响应体
         * @param progressListener 回调接口
         */
        public ProgressResponseBody(ResponseBody responseBody, ResponseListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
        }


        /**
         * 重写调用实际的响应体的contentType
         * @return MediaType
         */
        @Override public MediaType contentType() {
            return responseBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength
         * @return contentLength
         * @throws IOException 异常
         */
        @Override public long contentLength() {
            return responseBody.contentLength();
        }

        /**
         * 重写进行包装source
         * @return BufferedSource
         * @throws IOException 异常
         */
        @Override public BufferedSource source() {
            if (bufferedSource == null) {
                //包装
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        /**
         * 读取，回调进度接口
         * @param source Source
         * @return Source
         */
        private Source source(Source source) {

            return new ForwardingSource(source) {
                //当前读取字节数
                long totalBytesRead = 0L;
                @Override public long read(Buffer sink, long byteCount) throws IOException {
                    try {
                        long bytesRead = super.read(sink, byteCount);
                        //增加当前读取的字节数，如果读取完成了bytesRead会返回-1
                        totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                        //回调，如果contentLength()不知道长度，会返回-1
                        if(progressListener!=null) {
                            progressListener.onResponseProgress(totalBytesRead, responseBody.contentLength(), bytesRead == -1);
                        }
                        return bytesRead;
                    }catch (Exception e){
                        if(progressListener!=null){
                            if(!progressListener.onError(e)) {
                                throw e;
                            }
                        }else {
                            throw e;
                        }
                    }
                    return -1;
                }
            };
        }
    }

    //响应体进度回调接口，比如用于文件下载中
    //主要用于返回数据读取的监听
    public interface ResponseListener {
        void onResponseProgress(long bytesRead, long contentLength, boolean done);
        boolean onError(Exception e);
    }

    //请求体进度回调接口，比如用于文件上传中
    public interface RequestListener {
        void onRequestProgress(long bytesWritten, long contentLength, boolean done);
        boolean onError(Exception e);
    }
    //包装的请求体，处理进度
    public  class ProgressRequestBody extends RequestBody {
        //实际的待包装请求体
        private final RequestBody requestBody;
        //进度回调接口
        private final RequestListener progressListener;
        //包装完成的BufferedSink
        private BufferedSink bufferedSink;

        /**
         * 构造函数，赋值
         * @param requestBody 待包装的请求体
         * @param progressListener 回调接口
         */
        public ProgressRequestBody(RequestBody requestBody, RequestListener progressListener) {
            this.requestBody = requestBody;
            this.progressListener = progressListener;
        }

        /**
         * 重写调用实际的响应体的contentType
         * @return MediaType
         */
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength
         * @return contentLength
         * @throws IOException 异常
         */
        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        /**
         * 重写进行写入
         * @param sink BufferedSink
         * @throws IOException 异常
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                //包装
                bufferedSink = Okio.buffer(sink(sink));
            }
            //写入
            requestBody.writeTo(bufferedSink);
            //必须调用flush，否则最后一部分数据可能不会被写入
            bufferedSink.flush();

        }

        /**
         * 写入，回调进度接口
         * @param sink Sink
         * @return Sink
         */
        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                //当前写入字节数
                long bytesWritten = 0L;
                //总字节长度，避免多次调用contentLength()方法
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    try{
                        super.write(source, byteCount);
                        if (contentLength == 0) {
                            //获得contentLength的值，后续不再调用
                            contentLength = contentLength();
                        }
                        //增加当前写入的字节数
                        bytesWritten += byteCount;
                        if(progressListener!=null){
                            //回调
                            progressListener.onRequestProgress(bytesWritten, contentLength, bytesWritten == contentLength);
                        }
                    }catch (Exception e){
                        if(progressListener!=null){
                            boolean b = progressListener.onError(e);
                            if(!b) {
                                throw e;
                            }
                        }else{
                            throw e;
                        }
                    }
                }
            };
        }
    }

    public synchronized HttpRequest addRequestListener(RequestListener requestListener) {
        if(requestListeners==null){
            synchronized (HttpRequest.class){
                if(requestListeners==null) {
                    requestListeners = new ArrayList<>();
                }
            }
        }
        requestListeners.add(requestListener);
        return this;
    }

    public synchronized HttpRequest addResponseListener(ResponseListener responseListener) {
        if(responseListeners==null) {
            synchronized (HttpRequest.class) {
                if (responseListener == null) {
                    responseListeners = new ArrayList<>();
                }
            }
        }
        responseListeners.add(responseListener);
        return this;
    }

    public abstract static class UploadListener implements RequestListener{
        @Override
        public void onRequestProgress(long bytesWritten, long contentLength, boolean done) {
            if(done){
                onSuccess(contentLength);
            }else{
                onProgressChange(bytesWritten*1.0f/contentLength,contentLength,bytesWritten);
            }
        }

        public abstract void onSuccess(long allLength);

        public abstract void onProgressChange(float progress, long allLength, long upLenth);
    }

    public abstract static class OnUIUploadListener extends UploadListener{
        protected Activity activity;

        public OnUIUploadListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onError(final Exception e) {
            if(activity!=null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onUIError(e);
                    }
                });
                return true;
            }else {
                return false;
            }
        }

        @Override
        public void onSuccess(final long allLength) {
            if(activity!=null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onUISuccess(allLength);
                    }
                });
            }
        }

        @Override
        public void onProgressChange(final float progress, final long allLength, final long upLenth) {
            if(activity!=null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onUIProgressChange(progress,allLength,upLenth);
                    }
                });
            }
        }

        public abstract void onUIProgressChange(float progress, long allLength, long upLenth);

        public abstract void onUISuccess(long allLength);

        public abstract void onUIError(Exception e);

    }

    public abstract static class OnHandlerUploadListener extends UploadListener{
        protected Handler handler;

        public OnHandlerUploadListener(Handler handler) {
            this.handler = handler;
        }

        @Override
        public boolean onError(final Exception e) {
            if(handler!=null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onUIError(e);
                    }
                });
                return true;
            }else {
                return false;
            }
        }

        @Override
        public void onSuccess(final long allLength) {
            if(handler!=null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onUISuccess(allLength);
                    }
                });
            }
        }

        @Override
        public void onProgressChange(final float progress, final long allLength, final long upLenth) {
            if(handler!=null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onUIProgressChnage(progress,allLength,upLenth);
                    }
                });
            }
        }

        public abstract void onUIProgressChnage(float progress, long allLength, long upLenth);

        public abstract void onUISuccess(long allLength);

        public abstract void onUIError(Exception e);

    }

    public abstract static class DownloadListener implements ResponseListener{
        @Override
        public void onResponseProgress(long bytesRead, long contentLength, boolean done) {
            if(done){
                onSuccess(contentLength);
            }else{
                onProgressChnage(bytesRead*1.0f/contentLength,contentLength,bytesRead);
            }
        }

        public abstract void onSuccess(long allLength);

        public abstract void onProgressChnage(float progress,long allLength,long downLenth);
    }

    public abstract static class OnUIDownloadListener extends DownloadListener{
        protected Activity activity;

        public OnUIDownloadListener(Activity activity) {
            this.activity = activity;
        }

        @Override
        public boolean onError(final Exception e) {
            if(activity!=null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onUIError(e);
                    }
                });
                return true;
            }else {
                return false;
            }
        }

        @Override
        public void onSuccess(final long allLength) {
            if(activity!=null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onUISuccess(allLength);
                    }
                });
            }
        }

        @Override
        public void onProgressChnage(final float progress, final long allLength, final long downLenth) {
            if(activity!=null){
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onUIProgressChnage(progress,allLength,downLenth);
                    }
                });
            }
        }

        public abstract void onUIProgressChnage(float progress, long allLength, long downLenth);

        public abstract void onUISuccess(long allLength);

        public abstract void onUIError(Exception e);

    }

    /**
     * 请求异常
     */
    public static class RequestException extends RuntimeException{
        public RequestException() {
        }

        public RequestException(String message) {
            super(message);
        }
    }

    public static CheckNetworkCallback getCheckNetworkCallback() {
        return checkNetworkCallback;
    }

    public static void setCheckNetworkCallback(CheckNetworkCallback checkNetworkCallback) {
        HttpRequest.checkNetworkCallback = checkNetworkCallback;
    }

    public static void setSimplNetworkChecker(Context context) {
        setCheckNetworkCallback(new SimplNetworkChecker(context));
    }

    private static class SimplNetworkChecker implements CheckNetworkCallback{

        private Context context;

        public SimplNetworkChecker(Context context) {
            this.context = context;
        }

        @Override
        public boolean isNetworkConnected() {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isAvailable();
        }
    }

    public static void init(Context context){
        //初始化网络状态检查器
        setSimplNetworkChecker(context);

        isInit = context!=null;
    }

    public HttpRequest setGet(boolean get) {
        isGet = get;
        return this;
    }

    public HttpRequest setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }
}
