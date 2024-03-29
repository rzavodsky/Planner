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
import rzavodsky.planner.util.dpToPx
import kotlin.math.max


/**
 * View, which shows a list of items according to their starting hour and duration
 * This view requires an adapter, which can provide information about the displayed data
 */
open class DayView<T: DayView.ViewHolder>: ViewGroup {
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


    private val viewHolders = mutableListOf<T>()

    /**
     * Adapter, which will be used to provide information to this DayView
     */
    open var adapter: Adapter<T>? = null
        set(value) {
            field = value
            value?.changeNotifier = ::dataSetChanged
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


    private fun dataSetChanged() {
        if (adapter == null) {
            removeAllViews()
            return
        }

        val targetChildCount = adapter!!.getItemCount()

        if (viewHolders.size > targetChildCount) {
            while (viewHolders.size > targetChildCount) {
                removeView(viewHolders.last().view)
                viewHolders.removeLast()
            }
        } else if (viewHolders.size < targetChildCount) {
            while (viewHolders.size < targetChildCount) {
                val viewHolder = createChild(viewHolders.size)
                addView(viewHolder.view)
                viewHolders.add(viewHolder)
            }
        }

        for (i in 0 until viewHolders.size) {
            adapter!!.bindViewHolder(i, viewHolders[i])
        }
    }

    protected open fun createChild(i: Int): T {
        val viewHolder = adapter!!.createViewHolder(this)
        viewHolder.view.setOnClickListener {
            adapter?.onClick(i)
        }
        return viewHolder
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

        for (i in 0 until viewHolders.size) {
            layoutChild(i, adapter!!.getHourAt(i), adapter!!.getDurationAt(i))
        }
    }

    protected fun layoutChild(pos: Int, hour: Int, duration: Int) {
        val child = viewHolders[pos].view
        val top =  hour * hourHeight
        val bot = top + duration * hourHeight
        child.layout(left + sideSize.toInt(), top.toInt(), right, bot.toInt())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        for (i in 0 until viewHolders.size) {
            measureChild(viewHolders[i].view, widthMeasureSpec, heightMeasureSpec)
        }
        setMeasuredDimension(widthMeasureSpec, (hourHeight * 24).toInt())
    }

    /**
     * Stores the view, which will be displayed in a DayView
     */
    open class ViewHolder(val view: View)

    /**
     * Adapter, which provides information about data to a DayView
     */
    abstract class Adapter<T: ViewHolder> {
        var changeNotifier: (() -> Unit)? = null

        /**
         * Returns the amount of items to display in a DayView
         */
        abstract fun getItemCount(): Int
        /**
         * Returns the starting hour for the item at position pos
         */
        abstract fun getHourAt(pos: Int): Int
        /**
         * Returns the duration for the item at position pos
         */
        abstract fun getDurationAt(pos: Int): Int
        /**
         * Binds a ViewHolder to the item at position pos
         */
        abstract fun bindViewHolder(pos: Int, view: T)
        /**
         * Creates a new ViewHolder
         */
        abstract fun createViewHolder(parent: ViewGroup): T

        /**
         * Called, when the view associated with item at pos is clicked
         */
        abstract fun onClick(pos: Int)

        /**
         * Notifies the DayView of changes in the dataset
         */
        fun notifyDatasetChanged() {
            changeNotifier?.invoke()
        }
    }
}
