package rzavodsky.planner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dayView = findViewById<EditableDayView>(R.id.day_view)
        val adapter = PlanBlockAdapter()
        adapter.data.add(PlanBlock(1, 2, "Test"))
        adapter.data.add(PlanBlock(4, 2, "Test2"))
        dayView.editableAdapter = adapter
    }
}