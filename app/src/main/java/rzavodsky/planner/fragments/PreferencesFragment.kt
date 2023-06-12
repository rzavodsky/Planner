package rzavodsky.planner.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import rzavodsky.planner.Preferences
import rzavodsky.planner.R

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
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<Preference>("org_file")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            orgFileIntentLauncher.launch(intent)
            true
        }
    }
}