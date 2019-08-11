package chat.rocket.android.thememanager.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.thememanager.BaseActivity
import chat.rocket.android.thememanager.adapter.CustomThemesAdapter
import chat.rocket.android.thememanager.adapter.ThemesAdapter
import chat.rocket.android.thememanager.model.Theme
import chat.rocket.android.thememanager.util.ThemeUtil
import kotlinx.android.synthetic.main.activity_themes.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.item_custom_themes_recycler.*
import kotlinx.android.synthetic.main.item_theme_info.*
import kotlinx.android.synthetic.main.item_themes_recycler.*


fun newInstance() = ThemesActivity()

class ThemesActivity : BaseActivity() {
    private lateinit var adapter: ThemesAdapter
    private lateinit var customThemesAdapter: CustomThemesAdapter
    private lateinit var baseThemesAdapter: ArrayAdapter<String>
    private var baseThemeIndex: Int = 0
    private var baseThemesArray = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_themes)

        baseThemesArray = viewModel.getThemeNames()
        baseThemesAdapter = ArrayAdapter(
                this, R.layout.item_alert_dialog_single_choice, baseThemesArray)

        setupToolbar()
        setDate()
        setCurrentTheme()
        subscribeUi()
        setupListeners()
        setupRecyclerViewSwipeListener()
        setupExpandedRecyclerViews()
    }

    private fun setupExpandedRecyclerViews() {
        if (viewModel.getIsCustom()) {
            custom_themes_recycler_view.visibility = View.VISIBLE
            recycler_view.visibility = View.GONE
        } else {
            custom_themes_recycler_view.visibility = View.GONE
            recycler_view.visibility = View.VISIBLE
        }
    }

    private fun setDate() {
        theme_last_changed.text = getString(R.string.last_changed, viewModel.getSavedDate())
    }

    private fun setCurrentTheme() {
        current_theme.text = viewModel.getCurrentThemeName()
    }

    private fun subscribeUi() {
        viewModel.getThemes().observe(this, Observer { themes ->
            setupRecyclerView(themes)
        })
        viewModel.getCustomThemes().observe(this, Observer { customThemes ->
            setupCustomRecyclerView(customThemes)
        })
    }

    private fun saveTheme(theme: Theme) {
        viewModel.saveTheme(theme.toString())
        reloadActivity()
    }

    private fun reloadActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(Intent(this, ThemesActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        } else {
            recreate()
        }
    }

    private fun setupToolbar() {
        with((this as AppCompatActivity)) {
            with(toolbar) {
                setSupportActionBar(this)
                title = getString(R.string.title_change_theme)
                setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
                setNavigationOnClickListener { onBackPressed() }
            }
        }
    }

    private fun setupRecyclerView(themes: List<Theme>) {
        adapter = ThemesAdapter(themes, listener = { theme: Theme -> saveTheme(theme) })
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
    }

    private fun setupCustomRecyclerView(customThemes: List<Theme>) {
        customThemesAdapter = CustomThemesAdapter(customThemes,
                listener = { themeIndex: Int, baseThemeName -> saveCustomTheme(themeIndex, baseThemeName) },
                colorListener = { themeIndex: Int, title: String, isDark: Boolean -> launchColorPicker(themeIndex, title, isDark) })
        custom_themes_recycler_view.layoutManager = LinearLayoutManager(this)
        custom_themes_recycler_view.adapter = customThemesAdapter
    }

    private fun setupListeners() {
        revert_to_default.setOnClickListener {
            viewModel.saveTheme("AppTheme")
            reloadActivity()
        }
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
                val dialog = AlertDialog.Builder(it)
                        .setTitle("Add Theme")
                        .setView(view)
                        .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.cancel() }
                        .setPositiveButton(android.R.string.yes) { _, _ -> addCustomTheme(baseThemeIndex, editText.text.toString()) }
                        .create()
                dialog.show()
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ThemeUtil.getThemeColor(R.attr.colorAccent))
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ThemeUtil.getThemeColor(R.attr.colorAccent))
            }
        }
        text_app_themes.setOnClickListener {
            if (recycler_view.visibility == View.VISIBLE) {
                recycler_view.visibility = View.GONE
            } else {
                recycler_view.visibility = View.VISIBLE
                custom_themes_recycler_view.visibility = View.GONE
            }
        }
        text_custom_themes.setOnClickListener {
            if (custom_themes_recycler_view.visibility == View.VISIBLE) {
                custom_themes_recycler_view.visibility = View.GONE
            } else {
                custom_themes_recycler_view.visibility = View.VISIBLE
                recycler_view.visibility = View.GONE
            }
        }
        TooltipCompat.setTooltipText(tooltip, "Tap a theme name to apply.\nTap a palette color to edit.\nSwipe to remove palette.")
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
        custom_themes_recycler_view.visibility = View.VISIBLE
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
}
