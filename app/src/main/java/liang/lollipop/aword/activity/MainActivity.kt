package liang.lollipop.aword.activity

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import kotlinx.android.synthetic.main.activity_main.*
import liang.lollipop.aword.drawable.LBackDrawable
import liang.lollipop.aword.view.TouchListenerImageView
import java.util.*
import android.content.IntentFilter
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import liang.lollipop.aword.R
import liang.lollipop.aword.receiver.AlertReceiver
import liang.lollipop.aword.service.UpdateWordService
import liang.lollipop.aword.util.AlertUtil
import liang.lollipop.aword.util.WordDBUtil


/**
 * 主页，主要功能的展示页面，情书的显示页面
 * @author Lollipop
 */
class MainActivity : AppCompatActivity(), View.OnClickListener,ValueAnimator.AnimatorUpdateListener {

    //返回按钮的绘制对象
    private lateinit var backDrawable:LBackDrawable

    //翼展动画控制器
    private val wingsAnimator = ValueAnimator.ofFloat(0F,1F)

    //下一条按钮的动画进度
    private var nextBtnPressPro = 0F

    //常量
    companion object {

        //翼展动画完整时长
        private val WINGS_ANIMATOR_DURATION = 1000L

        //文本变化的动画时长
        private val TEXT_ANIMATOR_DURATION = 1000L

    }

    //回弹插值器
    private val bounceInterpolator = BounceInterpolator()

    //减速插值器
    private val decelerateInterpolator = DecelerateInterpolator()

    //内容集合
    private val wordArray:ArrayList<String> = ArrayList()

    //随机数
    private val random:Random = Random()

    //文字变化的过渡动画
    private val textAnimator = ValueAnimator.ofFloat(1F,0F)

    //日历类
    private val calendar:Calendar = Calendar.getInstance()

    //广播监听类，用于监听一些广播
    private val broadcastReceiver = object :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent==null){
                return
            }
            when(intent.action){
                Intent.ACTION_TIME_TICK,Intent.ACTION_TIMEZONE_CHANGED,Intent.ACTION_TIME_CHANGED -> {
                    setColor()
                }

                UpdateWordService.ACTION_UPDATE_SUCCESS -> {
                    initData()
                }
            }
        }
    }

    //初始化方法
    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initData()
        initView()
        initBroadcastReceiver()

        val intentWord = intent.getStringExtra(AlertReceiver.ARG_WORD)
        if(TextUtils.isEmpty(intentWord)){
            nextWord()
        }else{
            wordView.text = intentWord
        }

        startService(Intent(this,UpdateWordService::class.java))
    }

    override fun onStart() {
        super.onStart()
        hideSystemUI()
    }

    private fun hideSystemUI(){
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE
    }

    //初始化View
    private fun initView(){
        //初始化文字字体
        val typeFace = Typeface.createFromAsset(assets,"fonts/font.ttf")
        //设置字体
        wordView.typeface = typeFace

        //初始化返回按钮
        backDrawable = LBackDrawable(this)
        //禁用旋转
        backDrawable.setSpinEnabled(false)
        //默认闭合状态
        backDrawable.progress = 0F
        //设置样式为翼展模式
        backDrawable.backType = LBackDrawable.BackType.WINGS
        //关联返回按钮的样式与按钮
        nextBtn.setImageDrawable(backDrawable)
        //讲返回按钮旋转180°，作为下一步按钮
        nextBtn.rotation = 180F

        //设置默认的颜色为一半透明度的纯白色
        backDrawable.setColor(0x30FFFFFF)

        //设置返回按钮的监听器
        nextBtn.setOnClickListener(this)
        wordView.setOnClickListener(this)

        //初始化动画
        initAnimator()

        //添加手势监听，用于增加点击动画
        nextBtn.addOnTouchListener(object : TouchListenerImageView.OnTouchListener{
            override fun onTouch(v: View, event: MotionEvent?) {
                if(event==null){
                    return
                }
                when(event.action){

                    MotionEvent.ACTION_DOWN -> onNextBtnPress()

                    MotionEvent.ACTION_UP -> onNextBtnUp()

                }

            }
        })

        setColor()
    }

    private fun initData(){
        wordArray.clear()
        WordDBUtil.getReadableDatabase(this).getAll(wordArray).close()
        if(wordArray.isEmpty()){
            //初始化数据数组
            val defWords = resources.getStringArray(R.array.word_array)
            wordArray += defWords
        }
    }

    private fun initBroadcastReceiver(){
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_TIME_TICK)
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED)

        intentFilter.addAction(UpdateWordService.ACTION_UPDATE_SUCCESS)

        registerReceiver(broadcastReceiver, intentFilter)
    }

    private fun deleteBroadcastReceiver(){
        unregisterReceiver(broadcastReceiver)
    }

    //初始化动画控制器
    private fun initAnimator(){

        //初始化返回按钮的翼展动画，设置监听器
        wingsAnimator.addUpdateListener(this)
        //设置动画时间
        wingsAnimator.duration = WINGS_ANIMATOR_DURATION
        //设置插值器
        wingsAnimator.interpolator = bounceInterpolator

        //设置延时200毫秒之后开始动画
        nextBtn.postDelayed({ wingsAnimator.start() },200L)

        //为文本动画增加一个变化监听器
        textAnimator.addUpdateListener(this)
        //设置动画时长
        textAnimator.duration = TEXT_ANIMATOR_DURATION
        //增加一个插值器
        textAnimator.interpolator = decelerateInterpolator
        //设置重复2次
        textAnimator.repeatCount = 1
        //设置重复模式为反向
        textAnimator.repeatMode = ValueAnimator.REVERSE
        //
        textAnimator.addListener(object : Animator.AnimatorListener{
            override fun onAnimationRepeat(animation: Animator?) {
                val next = wordArray[random.nextInt(wordArray.size)]
                wordView.text = next
            }

            override fun onAnimationEnd(animation: Animator?) {}

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}

        })

    }

    //下一步按钮按下时，启动一个收齐动画
    private fun onNextBtnPress(){

        wingsAnimator.cancel()
        wingsAnimator.setFloatValues(nextBtnPressPro,0.5F)
        val duration = (Math.abs(nextBtnPressPro-0.5f)*2* WINGS_ANIMATOR_DURATION).toLong()
        wingsAnimator.duration = duration
        //设置插值器
        wingsAnimator.interpolator = decelerateInterpolator
        wingsAnimator.start()

    }

    //下一步按钮松开时，启动一个展开动画
    private fun onNextBtnUp(){

        wingsAnimator.cancel()
        wingsAnimator.setFloatValues(nextBtnPressPro,1F)
        val duration = (Math.abs(1F-nextBtnPressPro)*2* WINGS_ANIMATOR_DURATION).toLong()
        wingsAnimator.duration = duration
        //设置插值器
        wingsAnimator.interpolator = bounceInterpolator
        wingsAnimator.start()

    }

    //下一句话
    private fun nextWord(){
        //如果正在运行动画，那么就放弃本次操作
        if(textAnimator.isRunning){
            return
        }

        textAnimator.start()

    }

    //动画更新时候的回调函数
    override fun onAnimationUpdate(animation: ValueAnimator?) {

        when(animation){

            wingsAnimator -> {

                nextBtnPressPro = animation!!.animatedValue as Float
                backDrawable.progress = nextBtnPressPro

            }

            textAnimator -> {

                val value = animation!!.animatedValue as Float
                wordView.alpha = value

            }

        }

    }

    //点击事件
    override fun onClick(v: View?) {
        when(v){

            nextBtn -> nextWord()

            wordView -> hideSystemUI()

            else -> {

            }

        }
    }

    //设置颜色
    private fun setColor(){
        val bgColor = getNowColor(
                ContextCompat.getColor(this, R.color.bgColorMax),
                ContextCompat.getColor(this, R.color.bgColorMin))
        val textColor = getNowColor(
                ContextCompat.getColor(this, R.color.textColorMax),
                ContextCompat.getColor(this, R.color.textColorMin))

        bgView.setBackgroundColor(bgColor)
        wordView.setTextColor(textColor)
    }

    //获取当前的颜色
    private fun getNowColor(maxColor:Int,minColor:Int):Int{
        //获取此刻的时间，并且为日历类赋值
        calendar.timeInMillis = System.currentTimeMillis()
        //得到小时的值，越接近中午12点，值越小，代表颜色越亮
        val hour = Math.abs(calendar.get(Calendar.HOUR_OF_DAY) - 12)
        //得到分钟的值，使颜色尽量的精确
        val minute = calendar.get(Calendar.MINUTE)

        //得到颜色分量的具体值
        val r = Color.red(maxColor) - Color.red(minColor)
        val g = Color.green(maxColor) - Color.green(minColor)
        val b = Color.blue(maxColor) - Color.blue(minColor)


        var redValue = 0
        if(r != 0){
            redValue = (1.0 * r * hour / 12).toInt()
            redValue += (1.0 * r / 12 * minute / 60).toInt()
        }
        redValue += Color.red(minColor)


        var greenValue = 0
        if(g != 0){
            greenValue = (1.0 * g * hour / 12).toInt()
            greenValue += (1.0 * g / 12 * minute / 60).toInt()
        }
        greenValue += Color.green(minColor)


        var blueValue = 0
        if(b != 0){
            blueValue = (1.0 * b * hour / 12).toInt()
            blueValue += (1.0 * b / 12 * minute / 60).toInt()
        }
        blueValue += Color.blue(minColor)

        return Color.rgb(redValue,greenValue,blueValue)

    }

    //销毁时的生命周期函数
    override fun onDestroy() {
        super.onDestroy()
        deleteBroadcastReceiver()
        val next = wordArray[random.nextInt(wordArray.size)]
        AlertUtil.alarmTo(this,next)
    }

}
