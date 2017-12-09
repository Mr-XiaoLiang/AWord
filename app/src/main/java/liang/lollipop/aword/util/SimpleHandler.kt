package liang.lollipop.aword.util

import android.os.Handler
import android.os.Message

/**
 * Created by lollipop on 2017/12/9.
 * @author Lollipop
 */
class SimpleHandler() : Handler(){

    private var callback: HandlerCallback? = null

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        if (callback != null) {
            callback!!.onHandler(msg)
        }
    }

    constructor (callback: HandlerCallback):this(){
        this.callback = callback
    }

    fun setCallback(callback: HandlerCallback) {
        this.callback = callback
    }

    interface HandlerCallback {
        fun onHandler(message: Message)
    }

}