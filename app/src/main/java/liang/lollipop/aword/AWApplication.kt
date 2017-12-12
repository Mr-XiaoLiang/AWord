package liang.lollipop.aword

import android.app.Application
import com.liang.lollipop.lhttprequest.HttpRequest
import liang.lollipop.aword.util.CrashHandler

/**
 * Created by lollipop on 2017/12/9.
 * @author Lollipop
 * 当前的应用上下文
 */
class AWApplication:Application() {

    override fun onCreate() {
        super.onCreate()
//        CrashHandler.init(this)
        HttpRequest.init(this)
    }

}