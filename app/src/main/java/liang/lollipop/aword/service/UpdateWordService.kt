package liang.lollipop.aword.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import com.liang.lollipop.lhttprequest.HttpRequest
import com.liang.lollipop.lhttprequest.TaskUtils
import liang.lollipop.aword.util.FileUtil
import liang.lollipop.aword.util.SimpleHandler
import liang.lollipop.aword.util.WordDBUtil
import okhttp3.Request
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.Exception


class UpdateWordService : Service(),SimpleHandler.HandlerCallback {

    private val handler:Handler = SimpleHandler(this)

    companion object {
        private val WORD_FILE_URL = "https://raw.githubusercontent.com/Mr-XiaoLiang/AWord/master/word/wordArray"

        private val WHAT_READ_WORD = 233

        val ACTION_UPDATE_SUCCESS = "AWORD_ACTION_UPDATE_SUCCESS"
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("not Bind")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        downWordFile()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun downWordFile(){
        val wordFile = FileUtil.getWordFile(this)
        if(wordFile.exists()){
            wordFile.delete()
        }
        HttpRequest
                .request(WORD_FILE_URL)
                .downloadTo(
                        FileUtil.getCachePath(this),
                        FileUtil.WORD_FILE,
                        object : HttpRequest.DownloadOnHandlerCallBack(handler){
                            override fun onUIError(code: Int, request: Request?, e: Exception?) {
                                //Do nothing
                                Log.e("downWordFile",if(e!=null)(e.message)else{"无错误描述"})
                            }

                            override fun onUIProgressChange(pro: Float, allLength: Long, downLength: Long) {
                                //Do nothing
                            }

                            override fun onUIDownLoadSuccess(path: String?) {
                                //下载完成，唤起读取服务
                                handler.sendEmptyMessage(WHAT_READ_WORD)
                            }
                        })
    }

    override fun onHandler(message: Message) {

        when(message.what){

            WHAT_READ_WORD -> readWordFile()

        }
    }

    private fun readWordFile(){
        //读取句子文件
        val wordFile = FileUtil.getWordFile(this)
        val wordDB = WordDBUtil.getWritableDatabase(this)
        TaskUtils.addTask(object :TaskUtils.CallBackForHandler<Boolean,Any>(handler){
            override fun onUISuccess(result: Boolean?) {
                if(result!=null && result){
                    onUpdateSuccess()
                }else{
                    onUpdateError()
                }
            }

            override fun onUIError(e: Exception?, code: Int, msg: String?) {
                Log.e("readWordFile",if(e!=null)(e.message)else{"无错误描述"} + code + "," + msg)
                onUpdateError()
            }

            override fun onBackground(vararg args: Any?): Boolean {
                val file:File = args[0] as File
                val db:WordDBUtil.SqlDB = args[1] as WordDBUtil.SqlDB

                //如果不存在，那么就删除
                if(!file.exists()){
                    return false
                }
                val br = BufferedReader(FileReader(file))

                var str: String? = br.readLine()

                val wordArray = ArrayList<String>()

                while (str != null) {

                    wordArray.add(str)

                    str = br.readLine()
                }

                br.close()

                if(!wordArray.isEmpty()){
                    db.addAll(wordArray).close()
                }

                return true
            }
        },wordFile,wordDB)
    }

    private fun onUpdateSuccess(){
        val intent = Intent()
        intent.action = ACTION_UPDATE_SUCCESS
        sendBroadcast(intent)
        stopSelf()
    }

    private fun onUpdateError(){
        stopSelf()
    }

}
