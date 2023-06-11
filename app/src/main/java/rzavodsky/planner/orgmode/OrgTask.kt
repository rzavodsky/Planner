package rzavodsky.planner.orgmode

import java.time.Instant

class OrgTask {
    var id: String? = null
    var title: String? = null
    var tags: List<String>? = null
    var priority: Char? = null
    var deadline: Instant? = null
    var scheduled: Instant? = null
    var description: String? = null
}
