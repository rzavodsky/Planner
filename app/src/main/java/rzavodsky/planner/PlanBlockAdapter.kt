package rzavodsky.planner

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import rzavodsky.planner.views.EditableDayView
import kotlin.random.Random

class PlanBlockAdapter: EditableDayView.Adapter {
    var data = mutableListOf<PlanBlock>()

    override fun getItemCount(): Int = data.size

    override fun getHourAt(pos: Int): Int = data[pos].hour

    override fun getDurationAt(pos: Int): Int = data[pos].duration

    override fun changeHourAt(pos: Int, hour: Int) {
        data[pos].hour = hour
    }

    override fun changeDurationAt(pos: Int, duration: Int) {
        data[pos].duration = duration
    }

    override fun initializeAt(pos: Int, view: View) {
        val bg = view.background as GradientDrawable
        val color = getBackgroundColor(data[pos].name)
        bg.colors = intArrayOf(color, color)

        view.findViewById<TextView>(R.id.task_name).text = data[pos].name
    }

    override fun createView(parent: ViewGroup): View {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.plan_block, parent, false)
        view.background.mutate()
        return view
    }

    private fun getBackgroundColor(name: String): Int {
        var seed = 0L
        for (c in name) {
            seed = seed * 31L + c.code
        }
        val rand = Random(seed)
        return Color.HSVToColor(floatArrayOf(rand.nextFloat() * 360, 0.6f, 1f))
    }
}