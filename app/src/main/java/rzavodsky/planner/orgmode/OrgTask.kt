package rzavodsky.planner.orgmode

import java.time.Instant
import java.time.temporal.ChronoUnit

class OrgTask {
    var _id: String? = null
    val id: String
        get() = _id!!
    var title: String? = null
    var tags: List<String>? = null
    var priority: Char? = null
    var deadline: Instant? = null
    var scheduled: Instant? = null
    var description: String? = null

    fun getInternalPriority(): Long {
        if (deadline != null) {
            return ChronoUnit.DAYS.between(Instant.now(), deadline)
        }
        if (scheduled != null) {
            return ChronoUnit.DAYS.between(Instant.now(), scheduled)
        }
        if (priority != null) {
            val prio = when (priority!!) {
                in '0'..'9' -> priority!! - '0'
                in 'A'..'Z' -> priority!! - 'A'
                else -> priority!!.code
            }
            return prio.toLong() + 100000
        }
        return Long.MAX_VALUE
    }
}
