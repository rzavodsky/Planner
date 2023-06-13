package rzavodsky.planner.orgmode

import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.ZoneId

class OrgParser(stream: InputStream) {
    private val taskRegex = Regex("""^\*+ +TODO +(?:\[#([A-Z0-9])] +)?(.*?)(?: +:([a-zA-Z0-9_@#%]+:)+)?$""")
    private val headingRegex = Regex("""^\*+ """)
    private val planningRegex = Regex("""(DEADLINE|SCHEDULED): *<(\d{4}-\d{2}-\d{2}) +(?:[a-zA-Z]+ +)?(\d{1,2}:\d{2})?[^>]*> *""")
    private val idPropertyRegex = Regex("""^:(?:ID|id): *(.+)?$""")
    private val reader = BufferedReader(InputStreamReader(stream))
    private var currentLine: String? = null

    fun parse(): List<OrgTask> {
        val tasks = mutableListOf<OrgTask>()
        currentLine = reader.readLine()
        while (currentLine != null) {
            tryParseTask()?.let {
                tasks.add(it)
            }
        }
        return tasks
    }

    private fun tryParseTask(): OrgTask? {
        if (currentLine == null) return null
        val match = taskRegex.matchEntire(currentLine!!)
        if (match == null) {
            currentLine = reader.readLine()
            return null
        }

        val task = OrgTask().apply {
            title = match.groups[2]?.value
            priority = match.groups[1]?.value?.first()
            tags = match.groups[3]?.value?.split(":")
        }

        Log.d("OrgParser", "Parsed ${task.title}")

        currentLine = reader.readLine()

        tryParsePlanning(task)
        tryParsePropertyDrawer(task)
        tryParseDescription(task)
        if (task._id == null) {
            Log.w("OrgParser", "Throwing out ${task.title}, no id")
            return null
        }

        return task
    }

    private fun tryParsePlanning(task: OrgTask, startAt: Int = 0): Boolean {
        if (currentLine == null) return false
        val match = planningRegex.matchAt(currentLine!!, startAt) ?: return false
        val type = match.groups[1]!!.value
        val date = match.groups[2]!!.value
        val time = match.groups[3]?.value ?: "00:00"

        val datetime = LocalDateTime.parse("${date}T${time}").atZone(ZoneId.systemDefault()).toInstant()
        when (type) {
            "DEADLINE" -> task.deadline = datetime
            "SCHEDULED" -> task.scheduled = datetime
        }

        if (startAt == 0) {
            tryParsePlanning(task, match.range.last + 1)
            currentLine = reader.readLine()
        }
        return true
    }

    private fun tryParsePropertyDrawer(task: OrgTask): Boolean {
        if (currentLine == null) return false
        if (currentLine!!.lowercase() != ":properties:") return false
        currentLine = reader.readLine()

        while (currentLine != null) {
            if (currentLine!!.lowercase() == ":end:") {
                currentLine = reader.readLine()
                return true
            }

            idPropertyRegex.matchEntire(currentLine!!)?.let { match ->
                match.groups[1]?.let { group ->
                    task._id = group.value
                }
            }

            currentLine = reader.readLine()
        }
        Log.w("OrgParser", "Reached EOF while parsing ${task.title}")
        return false
    }

    private fun tryParseDescription(task: OrgTask): Boolean {
        val description = StringBuilder()
        while (currentLine != null) {
            if (headingRegex.containsMatchIn(currentLine!!)) break
            description.appendLine(currentLine)
            currentLine = reader.readLine()
        }
        if (description.isNotEmpty()) {
            task.description = description.toString()
        }
        return true
    }
}