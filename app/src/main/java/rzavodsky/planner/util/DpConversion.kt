package rzavodsky.planner.util

import android.content.res.Resources
import android.util.TypedValue

val Float.dpToPx: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

val Int.dpToPx: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), Resources.getSystem().displayMetrics)
