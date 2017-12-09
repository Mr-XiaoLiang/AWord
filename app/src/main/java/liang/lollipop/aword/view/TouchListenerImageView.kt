package liang.lollipop.aword.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView

/**
 * Created by lollipop on 2017/12/9.
 * 支持手势监听的ImageView
 * @author Lollipop
 */
open class TouchListenerImageView(context:Context , attrs:AttributeSet? ,defStyleAttr:Int ,
                                  defStyleRes:Int ) : ImageView(context,attrs,defStyleAttr,defStyleRes) {

    constructor(context: Context,attrs: AttributeSet?,defStyleAttr: Int) : this(context,attrs,defStyleAttr,0)

    constructor(context: Context,attrs: AttributeSet?) : this(context,attrs,0)

    constructor(context: Context) : this(context,null)

    //手势监听的数组集合
    private val touchListenerArray:ArrayList<OnTouchListener> = ArrayList()


    interface OnTouchListener {
        fun onTouch(v: View, event: MotionEvent?)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        for(lis in touchListenerArray){
            lis.onTouch(this,event)
        }
        return super.onTouchEvent(event)
    }

    public fun addOnTouchListener(lis:OnTouchListener){
        touchListenerArray.add(lis)
    }

    public fun removeOnTouchListener(lis:OnTouchListener){
        touchListenerArray.remove(lis)
    }



}