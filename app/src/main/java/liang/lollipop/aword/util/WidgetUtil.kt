package liang.lollipop.aword.util

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import liang.lollipop.aword.R
import liang.lollipop.aword.widget.WordWidgetProvider

/**
 * Created by lollipop on 2017/12/11.
 * @author Lollipop
 * 用于更新小部件的工具类
 */
object WidgetUtil {

    fun update(context:Context,word:String,id:Int,appWidgetManager:AppWidgetManager){
        val views = RemoteViews(context.packageName, R.layout.widget_word)

        views.setTextViewText(R.id.widgetWordView,word)

        if(AWSettings.isShowNextBtn(context,id)){
            views.setViewVisibility(R.id.widgetNextBtn, View.VISIBLE)
            val intent = Intent(WordWidgetProvider.NEXT_ACTION)
            intent.putExtra(WordWidgetProvider.WIDGET_ID,id)
            val pendingIntent = PendingIntent.getBroadcast(context, WordWidgetProvider.NEXT_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.widgetNextBtn, pendingIntent)
        }else{
            views.setViewVisibility(R.id.widgetNextBtn, View.GONE)
        }

        if(AWSettings.isShowSendBtn(context,id)){
            views.setViewVisibility(R.id.widgetSendBtn, View.VISIBLE)
            val intent = createSMSIntent(word)
            val pendingIntent = PendingIntent.getActivity(context, WordWidgetProvider.NEXT_REQUEST, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.widgetSendBtn, pendingIntent)
        }else{
            views.setViewVisibility(R.id.widgetSendBtn, View.GONE)
        }

        appWidgetManager.updateAppWidget(id, views)
    }

    private fun createSMSIntent(smsBody: String):Intent {

        //"smsto:xxx" xxx是可以指定联系人的
        val smsToUri = Uri.parse("smsto:")

        val intent = Intent(Intent.ACTION_SENDTO, smsToUri)

        //"sms_body"必须一样，smsbody是发送短信内容content
        intent.putExtra("sms_body", smsBody)

        return intent

    }

}