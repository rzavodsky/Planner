package rzavodsky.planner.views

import android.content.ClipData
import android.content.ClipDescription
import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.DragEvent
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import rzavodsky.planner.R
import rzavodsky.planner.util.dpToPx
import kotlin.math.max
import kotlin.math.min

class EditableDayView<T: DayView.ViewHolder>: DayView<T> {
    private var drawable: GradientDrawable? = null

    private var dragging: Dragging? = null
    private var currentDraggingHour: Int? = null
    var editableAdapter: Adapter? = null

    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.EditableDayView)

        val resource = a.getResourceId(R.styleable.EditableDayView_dragging_drawable, 0)
        drawable = ResourcesCompat.getDrawable(resources, resource, null) as GradientDrawable

        a.recycle()
    }

    override fun onDragEvent(event: DragEvent?): Boolean {
        if (editableAdapter == null) return false
        when (event?.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                dragging = event.localState as Dragging
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                val i = dragging!!.pos
                dragging = null
                currentDraggingHour = null
                layoutChild(i, adapter!!.getHourAt(i), adapter!!.getDurationAt(i))
                invalidate()
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                val newDraggingHour = (event.y / hourHeight).toInt()
                if (currentDraggingHour != newDraggingHour) {
                    currentDraggingHour = newDraggingHour
                    val i = dragging!!.pos
                    if (dragging!!.move) {
                        setHour()
                        layoutChild(i, dragging!!.hour, adapter!!.getDurationAt(i))
                    } else {
                        setDuration()
                        val hour = adapter!!.getHourAt(i)
                        layoutChild(i, hour, dragging!!.hour - hour + 1)
                    }
                }
            }
            DragEvent.ACTION_DROP -> {
                if (dragging!!.move) {
                    setHour()
                    editableAdapter!!.changeHourAt(dragging!!.pos, dragging!!.hour)
                } else {
                    setDuration()
                    val i = dragging!!.pos
                    editableAdapter!!.changeDurationAt(i, dragging!!.hour - adapter!!.getHourAt(i) + 1)
                }
            }
        }
        return true
    }

    private fun findSpan(hour: Int, skipPos: Int? = null): Pair<Int, Int>? {
        var prevClosest = -1
        var nextClosest = 24
        for (i in 0 until adapter!!.getItemCount()) {
            if (i == skipPos) continue

            val otherHour = adapter!!.getHourAt(i)
            val otherDuration = adapter!!.getDurationAt(i)
            val otherEnd = otherHour + otherDuration - 1
            if (hour in otherHour .. otherEnd) {
                return null
            }

            if (otherEnd in (prevClosest + 1) until hour) {
                prevClosest = otherEnd
            }
            if (otherHour in (hour + 1) until nextClosest) {
                nextClosest = otherHour
            }
        }
        return prevClosest to nextClosest
    }
    private fun setDuration() {
        val currentHour = adapter!!.getHourAt(dragging!!.pos)
        val (_, nextHour) = findSpan(currentHour, dragging!!.pos)!!

        dragging!!.hour = min(max(currentDraggingHour!!, currentHour), nextHour - 1)
    }

    private fun setHour() {
        val duration = adapter!!.getDurationAt(dragging!!.pos)
        val span = findSpan(currentDraggingHour!!, dragging!!.pos) ?: return
        if (currentDraggingHour!! > span.first && currentDraggingHour!! < span.second - duration + 1) {
            dragging!!.hour = currentDraggingHour!!
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (dragging != null && dragging!!.move && drawable != null) {
            val i = dragging!!.pos
            val hour = adapter!!.getHourAt(i)
            val duration = adapter!!.getDurationAt(i)
            val top = hour * hourHeight
            val bottom = top + duration * hourHeight

            drawable!!.setStroke(3.dpToPx.toInt(), editableAdapter!!.getColor(i))
            drawable!!.setBounds(sideSize.toInt(), top.toInt(), width, bottom.toInt())
            drawable!!.draw(canvas!!)
        }
    }

    override fun createChild(i: Int): T {
        val viewHolder = super.createChild(i)
        viewHolder.view.setOnClickListener(null)
        val gd = GestureDetector(context, LongClickGestureDetector(viewHolder.view, i, adapter!!))
        viewHolder.view.setOnTouchListener {v, event ->
            gd.onTouchEvent(event)
            v.performClick()
            true
        }
        return viewHolder
    }

    interface Adapter {
        fun changeHourAt(pos: Int, hour: Int)
        fun changeDurationAt(pos: Int, duration: Int)
        fun getColor(pos: Int): Int
    }

    private class Dragging(val pos: Int, var hour: Int, val move: Boolean)

    private class EmptyShadowBuilder: DragShadowBuilder() {
        override fun onDrawShadow(canvas: Canvas?) { }
        override fun onProvideShadowMetrics(
            outShadowSize: Point?,
            outShadowTouchPoint: Point?
        ) {
            outShadowSize?.set(1, 1)
            outShadowTouchPoint?.set(0, 0)
        }
    }

    private class LongClickGestureDetector<T: ViewHolder>(
        private val view: View,
        private val pos: Int,
        private val adapter: DayView.Adapter<T>,
        ): GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            Log.d("DayView", "Moving item $pos")
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            val item = ClipData.Item(view.tag as? CharSequence)
            val dragData = ClipData(
                view.tag as? CharSequence,
                arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN),
                item
            )
            view.startDragAndDrop(dragData,
                EmptyShadowBuilder(),
                Dragging(pos, adapter.getHourAt(pos), e.y < (view.height / 2)), 0)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            Log.d("DayView", "Opening $pos")
            adapter.onClick(pos)
            return true
        }
    }
}