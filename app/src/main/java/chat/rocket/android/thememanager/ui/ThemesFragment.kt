package chat.rocket.android.thememanager.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import chat.rocket.android.R
import chat.rocket.android.thememanager.viewmodel.ThemesViewModel
import chat.rocket.android.thememanager.viewmodel.ThemesViewModelFactory
import chat.rocket.android.util.extensions.inflate
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_theme.*
import javax.inject.Inject


internal const val TAG_THEME_FRAGMENT = "ThemesFragment"

fun newInstance() = ThemesFragment()

class ThemesFragment : Fragment() {
    @Inject
    lateinit var factory: ThemesViewModelFactory
    private lateinit var viewModel: ThemesViewModel
    var currentTheme:String = "AppTheme"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this, factory).get(ThemesViewModel::class.java)
        currentTheme=viewModel.getCurrentTheme()!!
        applyTheme(activity)
        return container?.inflate(R.layout.fragment_theme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
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

    private fun applyTheme(activity: FragmentActivity?){
        println("Applying"+viewModel.getCurrentTheme()!!)
        if (currentTheme == "AppTheme"){
            activity?.setTheme(R.style.AppTheme)
        }
        else if(currentTheme == "DarkTheme"){
            activity?.setTheme(R.style.DarkTheme)
        }
        else if(currentTheme == "BlackTheme") {
            activity?.setTheme(R.style.BlackTheme)
        }
    }

    private fun setSavedTheme() {
        if(toggleButton!=null){
            if(viewModel.getLeftToggle()!!){
                toggleButton.isChecked=true
            }
        }
        if(toggleButtonLibrary!=null){
            if(viewModel.getRightToggle()!!){
                toggleButtonLibrary.isChecked=true
            }
        }
    }

    private fun setupListeners(){
        println("SetupListeners!")
        toggleButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if(buttonView.isPressed){
                if (isChecked) {
                    println("Left Checked" + toggleButton.isChecked+viewModel.getLeftToggle()+isChecked)
                    println("Right" + toggleButtonLibrary.isChecked+viewModel.getRightToggle()+isChecked)
                    toggleButtonLibrary.isChecked=false
                    viewModel.saveTheme("DarkTheme")
                } else {
                    viewModel.saveTheme("AppTheme")
                }
                reloadFragment()
            }
        }

        toggleButtonLibrary.setOnCheckedChangeListener { buttonView, isChecked ->
            if(buttonView.isPressed){
                if (isChecked) {
                    println("Right Checked" + toggleButtonLibrary.isChecked+viewModel.getRightToggle()+isChecked)
                    println("Left" + toggleButton.isChecked+viewModel.getLeftToggle()+isChecked)
                    toggleButton.isChecked=false
                    viewModel.saveTheme("BlackTheme")
                } else {
                    viewModel.saveTheme("AppTheme")
                }
                reloadFragment()
            }
        }
    }

    private fun reloadFragment() {
        println("Reload Fragment!")
        fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
    }
}
