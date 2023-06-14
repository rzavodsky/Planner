package rzavodsky.planner

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import rzavodsky.planner.database.PlanBlockDatabase
import rzavodsky.planner.databinding.ActivityMainBinding
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

const val NOTIFICATION_CHANNEL_ID = "channel_main"

/**
 * Main activity of this application. Handles navigation and switching between fragments.
 * This activity is also responsible for creation of Alarms
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Tasks.hasInstance()) {
            Tasks.getInstance().update(applicationContext)
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = binding.navHostFragment.getFragment<NavHostFragment>().navController
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)
        NavigationUI.setupWithNavController(binding.navDrawer, navController)

        createNotificationChannel()

        val dao = PlanBlockDatabase.getInstance(applicationContext).planBlockDao
        dao.getAllPlansForDay(LocalDate.now()).observe(this) {
            val alarmManager =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    ?: return@observe

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                while (!alarmManager.canScheduleExactAlarms()) {
                    Log.w("MainActivity", "No permission to schedule alarms")
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                }
            }

            val notifyAdvance = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getInt(Preferences.notifyAdvance, 0)

            for (block in it) {
                val time = LocalTime.of(block.hour, 0)
                    .atDate(LocalDate.now())
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .minusSeconds((60 * notifyAdvance).toLong())
                if (time < Instant.now()) {
                    Log.d("MainActivity", "Skipping time $time")
                    continue
                }
                Log.d("MainActivity", "Adding alarm for $time")
                val intent = Intent(this, NotificationBroadcastReceiver::class.java)
                intent.putExtra("hour", block.hour)
                val pendingIntent = PendingIntent.getBroadcast(
                    this,
                    block.hour,
                    intent,
                    PendingIntent.FLAG_MUTABLE
                )
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.toEpochMilli(), pendingIntent)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, "Plans", NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onPause() {
        super.onPause()
        SmallAppWidgetProvider.updateWidgets(applicationContext)
    }
    override fun onDestroy() {
        super.onDestroy()
        Tasks.teardown()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, binding.drawerLayout)
    }
}