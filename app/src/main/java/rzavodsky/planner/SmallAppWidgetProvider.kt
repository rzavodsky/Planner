package rzavodsky.planner

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import rzavodsky.planner.database.PlanBlockDatabase
import java.time.LocalDate
import java.time.LocalTime

/**
 * Widget, which shows the current or next plan block.
 */
class SmallAppWidgetProvider: AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob()).launch {
            val block = PlanBlockDatabase
                .getInstance(context!!)
                .planBlockDao
                .getNextPlan(LocalDate.now(), LocalTime.now().hour)

            val name = block?.let {
                if (it.isTaskPlan) {
                    Tasks.singleParse(context)[it.taskId!!]?.title ?: "Unknown"
                } else {
                    it.title!!
                }
            } ?: ""

            val time = block?.let {
                "${block.hour}:00 - ${block.hour + block.duration}:00"
            } ?: ""

            val header = block?.let {
                if (it.hour > LocalTime.now().hour) {
                    context.resources.getString(R.string.next_plan)
                } else {
                    context.resources.getString(R.string.current_plan)
                }
            } ?: context.resources.getString(R.string.no_tasks)

            appWidgetIds?.forEach { appWidgetId ->
                val pendingIntent = PendingIntent.getActivity(context, 0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

                val views = RemoteViews(context.packageName, R.layout.widget_small).apply {
                    setTextViewText(R.id.taskNameText, name)
                    setTextViewText(R.id.timeText, time)
                    setTextViewText(R.id.header, header)
                    setOnClickPendingIntent(R.id.container, pendingIntent)
                }
                appWidgetManager?.updateAppWidget(appWidgetId, views)
            }
            pendingResult.finish()
        }
    }

    companion object {
        /**
         * Sends a broadcast to manually update all widgets of this kind
         */
        fun updateWidgets(context: Context) {
            val widgetIntent = Intent(context, SmallAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, SmallAppWidgetProvider::class.java))
            manager.notifyAppWidgetViewDataChanged(ids, android.R.id.list)
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(widgetIntent)
        }
    }
}