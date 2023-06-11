package rzavodsky.planner.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import rzavodsky.planner.R
import rzavodsky.planner.dpToPx
import kotlin.math.max


open class DayView: ViewGroup {
    protected var hourHeight = 75.dpToPx
    private var hourPadding = 5.dpToPx
    private var sidePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 1.5f.dpToPx
    }
    private val separatorPaint = Paint(sidePaint).apply {
        pathEffect = DashPathEffect(floatArrayOf(1.5f.dpToPx, 1.5f.dpToPx), 0f)
    }
    private val timePaint = Paint().apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    protected var sideSize = 0f
    private var textHeight = 0


    open var adapter: Adapter? = null
        set(value) {
            field = value
            dataSetChanged()
        }

    constructor(context: Context): super(context) {
        calcTextSizes()
        this.setWillNotDraw(false)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DayView)

        hourHeight = a.getDimension(R.styleable.DayView_hour_height, hourHeight)
        separatorPaint.color = a.getColor(R.styleable.DayView_separator_color, separatorPaint.color)
        sidePaint.color = separatorPaint.color
        timePaint.color = a.getColor(R.styleable.DayView_hour_color, timePaint.color)
        timePaint.textSize = a.getDimension(R.styleable.DayView_hour_size, timePaint.textSize)
        hourPadding = a.getDimension(R.styleable.DayView_hour_padding, hourPadding)

        a.recycle()
        calcTextSizes()
        this.setWillNotDraw(false)
    }

    private fun calcTextSizes() {
        var maxTimeSize = 0f
        for (hour in 0..23) {
            maxTimeSize = max(timePaint.measureText("$hour:00"), maxTimeSize)
        }
        sideSize = maxTimeSize + hourPadding

        val bounds = Rect()
        timePaint.getTextBounds("00:00", 0, 5, bounds)
        textHeight = bounds.height()
    }


    fun dataSetChanged() {
        if (adapter == null) {
            removeAllViews()
            return
        }

        val targetChildCount = adapter!!.getItemCount()

        if (childCount > targetChildCount) {
            for (i in targetChildCount until childCount) {
                removeViewAt(i)
            }
        } else if (childCount < targetChildCount) {
            for (i in childCount until targetChildCount) {
                addView(createChild(i))
            }
        }

        for (i in 0 until childCount) {
            adapter!!.initializeAt(i, getChildAt(i))
        }
    }

    protected open fun createChild(i: Int): View {
        return adapter!!.createView(this)
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawLine(sideSize, 0f, sideSize, height.toFloat(), sidePaint)
        for (hour in 1..23) {
            val y = hour * hourHeight
            canvas?.drawLine(sideSize - 15, y, width.toFloat(), y, separatorPaint)
            canvas?.drawText("$hour:00", sideSize / 2, y + textHeight / 2, timePaint)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (adapter == null) return

        for (i in 0 until childCount) {
            layoutChild(i, adapter!!.getHourAt(i), adapter!!.getDurationAt(i))
        }
    }

    protected fun layoutChild(pos: Int, hour: Int, duration: Int) {
        val child = getChildAt(pos)
        val top =  hour * hourHeight
        val bot = top + duration * hourHeight
        child.layout(left + sideSize.toInt(), top.toInt(), right, bot.toInt())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (i in 0 until childCount) {
            measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec)
        }
        setMeasuredDimension(widthMeasureSpec, (hourHeight * 24).toInt())
    }

    interface Adapter {
        fun getItemCount(): Int
        fun getHourAt(pos: Int): Int
        fun getDurationAt(pos: Int): Int
        fun initializeAt(pos: Int, view: View)
        fun createView(parent: ViewGroup): View
    }
}