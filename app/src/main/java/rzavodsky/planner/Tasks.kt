package rzavodsky.planner

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import rzavodsky.planner.orgmode.OrgParser
import rzavodsky.planner.orgmode.OrgTask
import java.io.File
import java.io.FileNotFoundException

class OrgFileObserver(val tasks: Tasks, path: String, val context: Context): FileObserver(path, MODIFY) {
    override fun onEvent(event: Int, path: String?) {
        Log.i("OrgFileObserver", "File modified")
        tasks.update(context)
    }
}

class Tasks(looper: Looper?) {
    private var taskMap = MutableLiveData(HashMap<String, OrgTask>())
    private var handler = if (looper != null) Handler(looper) else null
    private var observer: FileObserver? = null
    private var observedPath: String? = null
    private var updateScheduled = false

    val tasks: LiveData<HashMap<String, OrgTask>>
        get() = taskMap

    fun update(context: Context) {
        if (Looper.myLooper() == handler?.looper) {
            doUpdate(context)
        } else {
            if (updateScheduled) return
            updateScheduled = true
            handler?.postDelayed({
                updateScheduled = false
                doUpdate(context)
            }, 1000)
        }
    }

    private fun doUpdate(context: Context) {
        val path = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(Preferences.orgFile, null)
        if (path == null) {
            taskMap.value = HashMap()
            observer?.stopWatching()
            return
        }

        val uri = Uri.parse(path)
        if (observedPath != path && handler != null) {
            observer?.stopWatching()
            setupObserver(context, uri)
        }

        reparseFile(context, uri)?.let {
            taskMap.value = it
        }
    }

    private fun setupObserver(context: Context, uri: Uri) {
        val file = File(Environment.getExternalStorageDirectory(), uri.path!!.split(':').last())
        observer = OrgFileObserver(this, file.canonicalPath, context)
        observer!!.startWatching()
    }

    fun removeObserver() {
        observer?.stopWatching()
        observer = null
    }


    private fun reparseFile(context: Context, uri: Uri): HashMap<String, OrgTask>? {
        val map = HashMap<String, OrgTask>()
        try {
            context.contentResolver.openInputStream(uri).use {
                if (it == null) {
                    return HashMap()
                }
                val parsedTasks = OrgParser(it).parse()
                for (task in parsedTasks) {
                    map[task.id] = task
                }
            }
            return map
        } catch (e: FileNotFoundException) {
            return null
        } catch (e: SecurityException) { // No longer have permission
            val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
            prefs.remove(Preferences.orgFile)
            prefs.apply()
            return HashMap()
        }
    }

    fun getTask(id: String): OrgTask? = taskMap.value!![id]

    companion object {
        @Volatile
        private var INSTANCE: Tasks? = null

        @JvmStatic
        fun getInstance(): Tasks {
            if (INSTANCE == null) {
                INSTANCE = Tasks(Looper.myLooper()!!)
            }
            return INSTANCE!!
        }
        fun hasInstance(): Boolean {
            return INSTANCE != null
        }
        fun singleParse(context: Context): HashMap<String, OrgTask> {
            val tasks = Tasks(null)
            val path = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Preferences.orgFile, null) ?: return HashMap()
            val uri = Uri.parse(path)
            return tasks.reparseFile(context, uri) ?: HashMap()
        }
        fun teardown() {
            INSTANCE?.removeObserver()
            INSTANCE = null
        }
    }
}