package liang.lollipop.aword.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import liang.lollipop.aword.R
import liang.lollipop.aword.activity.MainActivity
import liang.lollipop.aword.receiver.AlertReceiver
import java.util.*


/**
 * Created by lollipop on 2017/12/12.
 * @author Lollipop
 * 消息提醒的工具类
 */
object AlertUtil {

    private val CHANNEL_ID = "aword"

    fun alarmTo(context: Context,word:String){
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val random = Random()
        var nextMin:Long = (random.nextInt(60)+ 30).toLong()
        nextMin *= 60*1000
        nextMin += System.currentTimeMillis()

        val intent = Intent(context, AlertReceiver::class.java)
        intent.putExtra(AlertReceiver.ARG_WORD,word)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarm.set(AlarmManager.RTC_WAKEUP,nextMin,pendingIntent)
    }

    fun showNotification(context: Context,word: String){
        val build =  NotificationCompat.Builder(context,CHANNEL_ID)
        build.setContentText(word)
        build.setContentTitle(context.getString(R.string.notification_title))
        build.setSmallIcon(R.drawable.ic_email_black_24dp)
        build.setLargeIcon(BitmapFactory.decodeResource(context.resources,R.mipmap.ic_launcher_round))
        build.setAutoCancel(true)

        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(AlertReceiver.ARG_WORD,word)
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        val stackBuilder = TaskStackBuilder.create(context)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        build.setContentIntent(resultPendingIntent)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mId allows you to update the notification later on.
        mNotificationManager.notify(System.currentTimeMillis().toInt(), build.build())
    }

}