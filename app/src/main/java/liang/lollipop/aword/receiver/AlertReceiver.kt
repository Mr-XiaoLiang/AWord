package liang.lollipop.aword.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import liang.lollipop.aword.util.AlertUtil
import liang.lollipop.aword.util.WordDBUtil
import java.util.*

/**
 * Created by lollipop on 2017/12/12.
 * @author Lollipop
 * 定时提醒用的广播
 */
class AlertReceiver : BroadcastReceiver() {

    companion object {
        val ARG_WORD = "ARG_WORD"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        //创建一个消息，用于展示
        val word = intent!!.getStringExtra(ARG_WORD)
        if(!TextUtils.isEmpty(word)){
            AlertUtil.showNotification(context!!,word)
        }

        //创建一个新的计划，用于提醒
        val words = ArrayList<String>()
        WordDBUtil.getReadableDatabase(context!!).getAll(words).close()
        if(!words.isEmpty()){
            val random = Random()
            val nextWord = words[random.nextInt(words.size)]
            AlertUtil.alarmTo(context,nextWord)
        }

    }



}