package rzavodsky.planner

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import rzavodsky.planner.database.PlanBlockDatabase
import java.time.LocalDate

class NotificationBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        SmallAppWidgetProvider.updateWidgets(context!!)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob()).launch {
            val hour = intent?.getIntExtra("hour", -1)
            if (hour == null) {
                pendingResult.finish()
                return@launch
            }

            val dao = PlanBlockDatabase.getInstance(context).planBlockDao
            val nextPlan = dao.getNextPlan(LocalDate.now(), hour)
            if (nextPlan == null || nextPlan.hour != hour) {
                pendingResult.finish()
                return@launch
            }

            val title = if (nextPlan.isTaskPlan) {
                Tasks.singleParse(context)[nextPlan.taskId!!]?.title ?: "Unknown"
            } else {
                nextPlan.title
            }

            val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Planner")
                .setContentText(title)

            with(NotificationManagerCompat.from(context)) {
                notify(hour, builder.build())
            }

            pendingResult.finish()
        }
    }
}