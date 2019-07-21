package chat.rocket.android.thememanager.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.thememanager.BaseActivity
import chat.rocket.android.thememanager.adapter.CustomThemesAdapter
import chat.rocket.android.thememanager.model.Theme
import chat.rocket.android.thememanager.util.ThemeUtil
import kotlinx.android.synthetic.main.activity_create_themes.*
import kotlinx.android.synthetic.main.app_bar.*

class CreateThemesActivity : BaseActivity() {
    private lateinit var adapter: CustomThemesAdapter
    private lateinit var baseThemesAdapter: ArrayAdapter<String>
    private var baseThemeIndex: Int = 0
    private var baseThemesArray = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_themes)
        baseThemesArray = viewModel.getThemeNames()
        baseThemesAdapter = ArrayAdapter(
                this, R.layout.item_alert_dialog_single_choice, baseThemesArray)
        setupToolbar()
        subscribeUi()
        setupListeners()
        setupRecyclerViewSwipeListener()
    }

    private fun setupListeners() {
        layout_item_add_theme.setOnClickListener {
            let {
                val view = LayoutInflater.from(it).inflate(R.layout.dialog_add_theme, null)
                val listView = view.findViewById<ListView>(R.id.lv_items)
                listView?.adapter = baseThemesAdapter
                listView?.choiceMode = ListView.CHOICE_MODE_SINGLE
                listView?.setItemChecked(0, true)
                listView?.setOnItemClickListener { _, _, position, _ ->
                    baseThemeIndex = position
                }
                val editText = view.findViewById<EditText>(R.id.edit_text_theme_name)
                AlertDialog.Builder(it)
                        .setTitle("Add Theme")
                        .setView(view)
                        .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.cancel() }
                        .setPositiveButton(android.R.string.yes) { _, _ -> addCustomTheme(baseThemeIndex, editText.text.toString()) }
                        .create()
                        .show()
            }
        }
    }

    private fun addCustomTheme(baseThemeIndex: Int, customName: String) {
        val saved = viewModel.addCustomTheme(baseThemeIndex, customName)
        if (!saved) {
            let {
                AlertDialog.Builder(it)
                        .setTitle("Invalid Name")
                        .setMessage(ThemeUtil.getTintedString("Custom theme \"$customName\" already exists. Please choose a different name for your theme.", R.attr.colorPrimaryText))
                        .setPositiveButton(android.R.string.yes) { dialog, _ -> dialog.dismiss() }
                        .create()
                        .show()
            }
        }
    }

    private fun subscribeUi() {
        viewModel.getCustomThemes().observe(this, Observer { customThemes ->
            setupRecyclerView(customThemes)
        })
    }

    private fun setupRecyclerView(customThemes: List<Theme>) {
        adapter = CustomThemesAdapter(customThemes,
                listener = { themeIndex: Int, baseThemeName -> saveCustomTheme(themeIndex, baseThemeName) },
                colorListener = { themeIndex: Int, title: String, isDark: Boolean -> launchColorPicker(themeIndex, title, isDark) })
        custom_themes_recycler_view.layoutManager = LinearLayoutManager(this)
        custom_themes_recycler_view.adapter = adapter
    }

    private fun setupRecyclerViewSwipeListener() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val isCurrent = viewModel.removeCustomTheme(position)
                if (isCurrent) {
                    reloadActivity()
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(custom_themes_recycler_view)
    }

    private fun launchColorPicker(themeIndex: Int, title: String, isDark: Boolean) {
        var customColorsArray = arrayOf<String>()
        when (title) {
            "Custom Accent" -> customColorsArray = resources.getStringArray(R.array.CustomAccent)
            "Custom Toolbar" -> if (isDark) customColorsArray = resources.getStringArray(R.array.DarkCustomToolbar)
            else customColorsArray = resources.getStringArray(R.array.LightCustomToolbar)
            "Custom Background" -> if (isDark) customColorsArray = resources.getStringArray(R.array.DarkCustomBackground)
            else customColorsArray = resources.getStringArray(R.array.LightCustomBackground)
        }
        val adapter = ArrayAdapter(
                this, R.layout.item_alert_dialog_single_choice, customColorsArray)
        let {
            AlertDialog.Builder(it)
                    .setTitle(title)
                    .setSingleChoiceItems(
                            adapter, 0
                    ) { dialog, option ->
                        editCustomTheme(themeIndex, option, title, customColorsArray)
                        dialog.dismiss()
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                    .create()
                    .show()
        }
    }

    private fun editCustomTheme(themeIndex: Int, option: Int, colorType: String, customColorsArray: Array<String>) {
        val isCurrent = viewModel.editCustomTheme(colorType, themeIndex, resources.getIdentifier(customColorsArray[option], "color",
                this.packageName))
        if (isCurrent) {
            saveCustomTheme(themeIndex, viewModel.getCurrentTheme().name)
        }
    }

    private fun saveCustomTheme(themeIndex: Int, baseThemeName: String) {
        viewModel.saveCustomTheme(themeIndex, baseThemeName)
        reloadActivity()
    }

    private fun setupToolbar() {
        with((this as AppCompatActivity)) {
            with(toolbar) {
                setSupportActionBar(this)
                title = getString(R.string.create_custom_theme)
                setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
                setNavigationOnClickListener { onBackPressed() }
            }
        }
    }

    private fun reloadActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(Intent(this, CreateThemesActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        } else {
            recreate()
        }
    }
}
