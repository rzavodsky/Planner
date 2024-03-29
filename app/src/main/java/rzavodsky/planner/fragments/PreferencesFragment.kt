package rzavodsky.planner.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.h6ah4i.android.preference.NumberPickerPreferenceCompat
import com.h6ah4i.android.preference.NumberPickerPreferenceDialogFragmentCompat
import rzavodsky.planner.Preferences
import rzavodsky.planner.R
import rzavodsky.planner.Tasks

const val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"

/**
 * Fragment, which allows user to set preferences
 */
class PreferencesFragment: PreferenceFragmentCompat() {
        private val orgFileIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val file = result.data?.data ?: return@registerForActivityResult

            context?.contentResolver?.takePersistableUriPermission(file, Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
            pref.edit().apply {
                putString(Preferences.orgFile, file.toString())
                apply()
            }
            setOrgFileSummary()
            Tasks.getInstance().update(requireContext())
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<Preference>(Preferences.orgFile)?.apply {
            setOnPreferenceClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                orgFileIntentLauncher.launch(intent)
                true
            }
        }
        setOrgFileSummary()
    }

    private fun setOrgFileSummary() {
        PreferenceManager
            .getDefaultSharedPreferences(requireContext())
            .getString(Preferences.orgFile, null)
            ?.let {
                Uri.parse(it).lastPathSegment?.split("/")?.last()
            }?.let {
                findPreference<Preference>(Preferences.orgFile)?.summary = "Currently set to $it"
            }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return
        }

        if (preference is NumberPickerPreferenceCompat) {
            val f = NumberPickerPreferenceDialogFragmentCompat.newInstance(preference.key)
            @Suppress("DEPRECATION")
            f.setTargetFragment(this, 0)
            f.show(parentFragmentManager, DIALOG_FRAGMENT_TAG)
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }
}