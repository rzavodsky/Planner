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

class EditableDayView<T: DayView.ViewHolder>: DayView<T> {
    private var drawable: GradientDrawable? = null

    private var dragging: Dragging? = null
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
                layoutChild(i, adapter!!.getHourAt(i), adapter!!.getDurationAt(i))
                invalidate()
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                val newDraggingHour = (event.y / hourHeight).toInt()
                if (dragging?.hour != newDraggingHour) {
                    dragging?.hour = newDraggingHour
                    val i = dragging!!.pos
                    if (!dragging!!.move) {
                        val hour = adapter!!.getHourAt(i)
                        val duration = max(dragging!!.hour - hour, 1)
                        layoutChild(i, hour, duration)
                    } else {
                        layoutChild(i, dragging!!.hour, adapter!!.getDurationAt(i))
                    }
                }
            }
            DragEvent.ACTION_DROP -> {
                if (dragging!!.move) {
                    editableAdapter!!.changeHourAt(dragging!!.pos, dragging!!.hour)
                } else {
                    val i = dragging!!.pos
                    val duration = max(1, dragging!!.hour - adapter!!.getHourAt(i))
                    Log.d("DayView", "Changing duration to $duration, hour: ${dragging!!.hour}")
                    editableAdapter!!.changeDurationAt(i, duration)
                }
            }
        }
        return true
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