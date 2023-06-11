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
import rzavodsky.planner.dpToPx
import kotlin.math.max

class EditableDayView: DayView {
    private var drawable: GradientDrawable? = null

    private var dragging: Dragging? = null
    var editableAdapter: Adapter? = null
        set(value) {
            field = value
            adapter = value
        }

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
            val bg = getChildAt(i).background as GradientDrawable

            drawable!!.setStroke(3.dpToPx.toInt(), bg.colors!![0])
            drawable!!.setBounds(sideSize.toInt(), top.toInt(), width, bottom.toInt())
            drawable!!.draw(canvas!!)
        }
    }

    override fun createChild(i: Int): View {
        val view = super.createChild(i)
        val gd = GestureDetector(context, LongClickGestureDetector(view, i, adapter!!.getHourAt(i)))
        view.setOnTouchListener {_, event ->
            gd.onTouchEvent(event)
            view.performClick()
            true
        }
        return view
    }

    interface Adapter : DayView.Adapter {
        fun changeHourAt(pos: Int, hour: Int)
        fun changeDurationAt(pos: Int, duration: Int)
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

    private class LongClickGestureDetector(private val view: View, private val pos: Int, private val hour: Int): GestureDetector.SimpleOnGestureListener() {
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
                Dragging(pos, hour, e.y < (view.height / 2)), 0)
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            Log.d("DayView", "Opening $pos")
            return true
        }
    }
}