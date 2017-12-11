package liang.lollipop.aword.util

import android.content.Context

/**
 * Created by lollipop on 2017/12/10.
 * @author Lollipop
 * 参数设置类
 */
object AWSettings {

    private val KET_SHOW_SEND_BTN = "KET_SHOW_SEND_BTN_"

    private val KET_SHOW_NEXT_BTN = "KET_SHOW_NEXT_BTN_"

    /**
     * 指定id的小部件是否显示发送信息的按钮
     */
    fun isShowSendBtn(context: Context,id:Int):Boolean{
        val value = SharedPreferencesUtils[context, KET_SHOW_SEND_BTN+id, true]
        return value ?: true
    }

    /**
     * 设置指定ID的小部件是否显示发送信息按钮
     */
    fun setShowSendBtn(context: Context,id:Int,value:Boolean){
        SharedPreferencesUtils.put(context,KET_SHOW_SEND_BTN+id,value)
    }

    /**
     * 指定id的小部件是否显示下一条按钮
     */
    fun isShowNextBtn(context: Context,id:Int):Boolean{
        val value = SharedPreferencesUtils[context, KET_SHOW_NEXT_BTN+id, true]
        return value ?: true
    }

    /**
     * 设置指定ID的小部件是否显示下一条按钮
     */
    fun setShowNextBtn(context: Context,id:Int,value:Boolean){
        SharedPreferencesUtils.put(context,KET_SHOW_NEXT_BTN+id,value)
    }

    fun copySendType(context: Context,newId:Int,oldId:Int){
        setShowSendBtn(context,newId, isShowSendBtn(context,oldId))
    }

    fun copyNextType(context: Context,newId:Int,oldId:Int){
        setShowNextBtn(context,newId, isShowNextBtn(context,oldId))
    }

    fun copyType(context: Context,newId:Int,oldId:Int){
        copySendType(context,newId,oldId)
        copyNextType(context,newId,oldId)
    }

}