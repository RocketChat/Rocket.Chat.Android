package chat.rocket.android.thememanager.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.chatrooms.viewmodel.ChatRoomsViewModelFactory
import chat.rocket.android.thememanager.InjectorUtils
import chat.rocket.android.thememanager.model.Theme
import chat.rocket.android.thememanager.viewmodel.ThemesViewModel
import chat.rocket.android.thememanager.viewmodel.ThemesViewModelFactory
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_theme.*
import kotlinx.android.synthetic.main.fragment_theme.view.*
import javax.inject.Inject



internal const val TAG_THEME_FRAGMENT = "ThemesFragment"

fun newInstance() = ThemesFragment()

class ThemesFragment : Fragment() {
//    @Inject lateinit var factory: ThemesViewModelFactory
    private lateinit var viewModel: ThemesViewModel
    var fragment: Fragment = this
    lateinit var sharedPref:SharedPreferences
    var currentTheme:String = "AppTheme"
    lateinit var factory:ThemesViewModelFactory
    var checked:Boolean = false
    var checkedLib:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        AndroidSupportInjection.inject(this)
        factory = InjectorUtils.provideThemesViewModelFactory()
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity()?.getApplicationContext())
        currentTheme = sharedPref.getString("current_theme", "AppTheme")
        checked = sharedPref.getBoolean("checked", false)
        checkedLib = sharedPref.getBoolean("checkedLib", false)
        setSavedTheme()
        applyTheme(sharedPref,activity)
    }

    private fun applyTheme(sharedPref:SharedPreferences,activity: FragmentActivity?){
        if (currentTheme == "AppTheme")
            activity?.setTheme(R.style.AppTheme)
        else if(currentTheme == "AppDarkTheme")
            activity?.setTheme(R.style.AppDarkTheme)
        else if(currentTheme == "LibraryTheme") {
            activity?.setTheme(R.style.LibraryTheme)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_theme)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this, factory).get(ThemesViewModel::class.java)
        val toggleButton = view.findViewById<ToggleButton>(R.id.toggleButton)
        val toggleButtonLibrary = view.findViewById<ToggleButton>(R.id.toggleButtonLibrary)
        subscribeUi()
        setSavedTheme()
        setupListeners()
    }

    private fun subscribeUi() {
            viewModel.getThemes().observe(this, Observer { themes ->
            val stringBuilder = StringBuilder()
            themes.forEach { theme ->
                stringBuilder.append("$theme\n\n")
            }
            textView_themes.text = stringBuilder.toString()
        })
    }

    private fun setSavedTheme() {
        if(toggleButton!=null){
            println("Toggle Exists!")
            if(checked)
                toggleButton.isChecked=true
        }
        if(toggleButtonLibrary!=null){
            println("Toggle Exists!")
            if(checkedLib)
                toggleButtonLibrary.isChecked=true
        }
    }

    private fun setupListeners(){
        toggleButton.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Toast.makeText(activity,"Turned On",Toast.LENGTH_LONG).show()
                toggleButtonLibrary.isChecked=false
                sharedPref.edit().putString("current_theme","AppDarkTheme").apply()
                sharedPref.edit().putBoolean("checked",true).apply()
                sharedPref.edit().putBoolean("checkedLib",false).apply()

            } else {
                Toast.makeText(activity,"Turned Off", Toast.LENGTH_LONG).show()
                sharedPref.edit().putString("current_theme","AppTheme").apply()
                sharedPref.edit().putBoolean("checked",false).apply()
            }
            reloadFragment()
        })

        toggleButtonLibrary.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Toast.makeText(activity,"Turned On",Toast.LENGTH_LONG).show()
                toggleButton.isChecked=false
                sharedPref.edit().putString("current_theme","LibraryTheme").apply()
                sharedPref.edit().putBoolean("checkedLib",true).apply()
                sharedPref.edit().putBoolean("checked",false).apply()
            } else {
                Toast.makeText(activity,"Turned Off", Toast.LENGTH_LONG).show()
                sharedPref.edit().putString("current_theme","AppTheme").apply()
                sharedPref.edit().putBoolean("checkedLib",false).apply()
            }
            reloadFragment()
        })
    }

    private fun reloadFragment() {
        fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
    }
    override fun onResume() {
        super.onResume();
        val theme = sharedPref.getString("current_theme", "AppTheme")
        checked = sharedPref.getBoolean("checked", false)
        checkedLib = sharedPref.getBoolean("checkedLib", false)
        if (currentTheme != theme)
            currentTheme=theme
            applyTheme(sharedPref,activity)
            setSavedTheme()
    }
}
