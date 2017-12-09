package liang.lollipop.aword.util

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.ArrayList

/**
 * Created by lollipop on 2017/12/9.
 * 句子的数据库操作类
 */
class WordDBUtil private constructor(context: Context): SQLiteOpenHelper(context,DB_NAME,null, VERSION) {

    companion object {

        private val DB_NAME = "CityDatabase"
        private val VERSION = 1

        private val TABLE_WORD = "WORD_TABLE"
        private val WORD = "WORD"

        val SELECT_WORD_SQL = " select $WORD from $TABLE_WORD ;"

        val CREATE_WORD_TABLE = "create table $TABLE_WORD ( $WORD varchar  );"

        fun getReadableDatabase(context: Context): SqlDB {
            return SqlDB(WordDBUtil(context), false)
        }

        fun getWritableDatabase(context: Context): SqlDB {
            return SqlDB(WordDBUtil(context), true)
        }

    }

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(CREATE_WORD_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    class SqlDB constructor(private var wordDatabaseHelper: WordDBUtil?, isWritable: Boolean) {
        private var sqLiteDatabase: SQLiteDatabase? = null

        init {
            if (isWritable) {
                this.sqLiteDatabase = wordDatabaseHelper!!.writableDatabase
            } else {
                this.sqLiteDatabase = wordDatabaseHelper!!.readableDatabase
            }
        }

        fun getAll(list: ArrayList<String>): SqlDB {
            list.clear()
            val sql = getSqLiteDatabase()
            val c = sql.rawQuery(SELECT_WORD_SQL, null)
            while (c.moveToNext()) {
                list.add(c.getString(c.getColumnIndex(WORD)))
            }
            c.close()
            return this
        }

        fun deleteAll(): SqlDB {
            getSqLiteDatabase().delete(TABLE_WORD, null, null)
            return this
        }

        fun addAll(list: ArrayList<String>?): SqlDB {
            deleteAll()
            if (list != null && !list.isEmpty()) {
                val sql = getSqLiteDatabase()
                sql.beginTransaction()
                try {
                    val values = ContentValues()
                    for (value in list) {
                        values.clear()
                        values.put(WORD, value)
                        sql.insert(TABLE_WORD, "", values)
                    }
                    sql.setTransactionSuccessful()
                } catch (e: Exception) {
                    Log.e("addAll", e.message)
                } finally {
                    sql.endTransaction()
                }
            }
            return this
        }

        fun close() {
            sqLiteDatabase!!.close()
            sqLiteDatabase = null
            wordDatabaseHelper!!.close()
            wordDatabaseHelper = null
        }

        private fun getSqLiteDatabase(): SQLiteDatabase {
            if (sqLiteDatabase == null) {
                throw RuntimeException("SQLiteDatabase was close")
            }
            return sqLiteDatabase!!
        }

    }

}