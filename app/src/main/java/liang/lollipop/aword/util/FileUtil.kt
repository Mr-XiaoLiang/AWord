package liang.lollipop.aword.util

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * Created by lollipop on 2017/12/9.
 * @author Lollipop
 * 文件处理相关的工具类
 */
object FileUtil {

    val WORD_FILE = "word"
    val SD_FOLDER_NAME = "AWord"

    fun getCachePath(context: Context): String {
        return context.cacheDir.path
    }

    fun getWordFile(context: Context): File {
        return File(getCachePath(context), WORD_FILE)
    }

    fun getSDLogPath(): String {
        return Environment.getExternalStorageDirectory().toString() + "/" + SD_FOLDER_NAME + "/log"
    }

}