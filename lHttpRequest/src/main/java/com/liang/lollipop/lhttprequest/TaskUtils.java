package com.liang.lollipop.lhttprequest;

import android.app.Activity;
import android.os.Handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by LiuJ on 2017/04/12.
 * 一个简陋的异步任务工具
 */
public class TaskUtils {

    /**
     * 线程池
     */
    private static final ThreadPoolExecutor threadPool;

    static {
        threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    private TaskUtils(){
    }

    private static class TaskUtilsHelper{
        private static TaskUtils taskUtils = new TaskUtils();
    }

    public static TaskUtils get(){
        return TaskUtilsHelper.taskUtils;
    }

    /**
     * 获取线程来执行任务
     * @param run 任务对象
     */
    public synchronized void runAs(Runnable run) {
        threadPool.execute(run);
    }

    public static class Task<E> implements Runnable{

        private CallBack callBack;
        private E[] args;

        @Override
        public void run() {
            if(callBack==null) {
                return;
            }
            try{
                callBack.success(callBack.processing(args));
            }catch (Exception e){
                callBack.error(e,0,e.getMessage());
            }
        }

        public Task(CallBack callBack, E... args) {
            this.callBack = callBack;
            this.args = args;
        }
    }

    public static void addTask(Task task){
        get().runAs(task);
    }

    public static <E> void addTask(CallBack callBack, E... args){
        get().runAs(new Task<E>(callBack,args));
    }

    public interface CallBack<T,E>{
        void success(T result);
        void error(Exception e, int code, String msg);
        T processing(E... args) throws Exception;
    }

    public static abstract class CallBackForHandler<T,E> implements CallBack<T,E> {

        protected Handler handler;

        public CallBackForHandler(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void success(final T result) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onUISuccess(result);
                }
            });
        }

        @Override
        public void error(final Exception e, final int code, final String msg) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onUIError(e,code,msg);
                }
            });
        }

        @Override
        public T processing(E... args) throws Exception {
            return onBackground(args);
        }
        public abstract void onUISuccess(T result);
        public abstract void onUIError(Exception e,int code,String msg);
        public abstract T onBackground(E... args) throws Exception;

    }

    public static abstract class OnUICallBack<T,E> implements CallBack<T,E>{

        protected Activity content;

        public OnUICallBack(Activity content) {
            this.content = content;
        }

        @Override
        public void success(final T result) {
            content.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onUISuccess(result);
                }
            });
        }

        @Override
        public void error(final Exception e, final int code, final String msg) {
            content.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onUIError(e,code,msg);
                }
            });
        }

        @Override
        public T processing(E... args) throws Exception {
            return onBackground(args);
        }
        public abstract void onUISuccess(T result);
        public abstract void onUIError(Exception e,int code,String msg);
        public abstract T onBackground(E... args) throws Exception;
    }

}
