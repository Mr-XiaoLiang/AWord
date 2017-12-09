package liang.lollipop.aword.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class UpdateWordService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("not Bind")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        downWordFile()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun downWordFile(){

    }

}
