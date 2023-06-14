package rzavodsky.planner.util

import android.content.res.Resources
import android.util.TypedValue

/**
 * Converts a float of device independent pixels into pixels
 */
val Float.dpToPx: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

/**
 * Converts an int of device independent pixels into pixels
 */
val Int.dpToPx: Float
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, toFloat(), Resources.getSystem().displayMetrics)
