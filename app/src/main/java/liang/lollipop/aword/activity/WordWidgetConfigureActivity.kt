package liang.lollipop.aword.activity

import android.app.Activity
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.CompoundButton
import android.widget.RemoteViews
import kotlinx.android.synthetic.main.activity_word_widget_configure.*
import kotlinx.android.synthetic.main.content_word_widget_configure.*
import kotlinx.android.synthetic.main.widget_word.*
import liang.lollipop.aword.R
import liang.lollipop.aword.util.AWSettings
import liang.lollipop.aword.util.WidgetUtil
import liang.lollipop.aword.util.WordDBUtil
import liang.lollipop.aword.widget.WordWidgetProvider
import java.util.*


class WordWidgetConfigureActivity : AppCompatActivity(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private var appWidgetId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word_widget_configure)
        setSupportActionBar(toolbar)

        enterFab.setOnClickListener(this)

        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        nextSwitch.isChecked = AWSettings.isShowNextBtn(this,appWidgetId)
        sendSwitch.isChecked = AWSettings.isShowSendBtn(this,appWidgetId)
        nextSwitch.setOnCheckedChangeListener(this)
        sendSwitch.setOnCheckedChangeListener(this)

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        when(buttonView){

            nextSwitch -> {
                widgetNextBtn.visibility = if(isChecked){View.VISIBLE}else{View.GONE}
                AWSettings.setShowNextBtn(this,appWidgetId,isChecked)
            }

            sendSwitch -> {
                widgetSendBtn.visibility = if(isChecked){View.VISIBLE}else{View.GONE}
                AWSettings.setShowSendBtn(this,appWidgetId,isChecked)
            }

        }

    }

    override fun onClick(v: View?) {

        when(v){

            enterFab -> {

                val appWidgetManager = AppWidgetManager.getInstance(this)

                val random = Random()

                val words = ArrayList<String>()
                WordDBUtil.getReadableDatabase(this).getAll(words).close()
                val word = if(words.isEmpty()){""}else{words[random.nextInt(words.size)]}

                WidgetUtil.update(this,word,appWidgetId,appWidgetManager)

                val resultValue = Intent()
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setResult(Activity.RESULT_OK, resultValue)
                finish()

            }

        }

    }

}
