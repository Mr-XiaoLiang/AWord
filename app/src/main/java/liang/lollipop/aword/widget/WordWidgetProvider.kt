package liang.lollipop.aword.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import liang.lollipop.aword.R
import liang.lollipop.aword.util.AWSettings
import liang.lollipop.aword.util.WidgetUtil
import liang.lollipop.aword.util.WordDBUtil
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by lollipop on 2017/12/10.
 * 小部件的控制类，也就是小部件的广播接收类
 * @author Lollipop
 */
class WordWidgetProvider : AppWidgetProvider(){

    private val random = Random()
    private val words = ArrayList<String>()

    companion object {
        val NEXT_ACTION = "android.appwidget.action.APPWIDGET_UPDATE"
        val NEXT_REQUEST = 666
        val WIDGET_ID = "WIDGET_ID"
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        words.clear()
        WordDBUtil.getReadableDatabase(context!!).getAll(words).close()

        for(id in appWidgetIds!!){

            val word = if(words.isEmpty()){""}else{words[random.nextInt(words.size)]}

            WidgetUtil.update(context,word,id,appWidgetManager!!)

        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        if(context==null||intent==null){
            return
        }
        when(intent.action){

            NEXT_ACTION -> {
                val id = intent.getIntExtra(WIDGET_ID,0)
                words.clear()
                WordDBUtil.getReadableDatabase(context).getAll(words).close()
                nextWord(context,id)

            }

        }
    }

    private fun nextWord(context: Context,id:Int){

        val appWidgetManager = AppWidgetManager.getInstance(context)

        val word = if(words.isEmpty()){""}else{words[random.nextInt(words.size)]}

        WidgetUtil.update(context,word,id,appWidgetManager)

    }

    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        super.onRestored(context, oldWidgetIds, newWidgetIds)
        if(context==null){
            return
        }
        for(index in 0..oldWidgetIds!!.size){
            AWSettings.copyType(context,newWidgetIds!![index],oldWidgetIds[index])
        }
    }


}