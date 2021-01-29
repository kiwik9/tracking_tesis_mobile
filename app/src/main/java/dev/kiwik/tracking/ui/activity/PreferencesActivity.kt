package dev.kiwik.tracking.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.google.android.flexbox.*
import dev.kiwik.tracking.R
import dev.kiwik.tracking.databinding.ActivityPreferencesBinding
import dev.kiwik.tracking.domain.api.LoginResponse
import dev.kiwik.tracking.domain.entities.Category
import dev.kiwik.tracking.domain.entities.CategoryCheck
import dev.kiwik.tracking.domain.entities.User
import dev.kiwik.tracking.domain.entities.UserRequest
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.ui.adapter.PreferenceAdapter
import dev.kiwik.tracking.utilities.InjectorUtils
import dev.kiwik.tracking.utilities.getDrawableCompat
import dev.kiwik.tracking.utilities.isNotNull
import dev.kiwik.tracking.utilities.isNull
import dev.kiwik.tracking.viewmodel.LoginViewModel
import dev.kiwik.tracking.viewmodel.ResourceViewModel
import kotlinx.coroutines.launch


class PreferencesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreferencesBinding
    private lateinit var user: User
    private lateinit var adapter: PreferenceAdapter
    private val loginViewModel: LoginViewModel by viewModels()

    private val preferencesViewModel: ResourceViewModel by viewModels {
        InjectorUtils.provideResourceViewModelFactory(applicationContext)
    }

    private val pref by lazy {
        Pref.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        window.statusBarColor = getColor(R.color.color_primary)
        setContentView(binding.root)
        setButtonFunction()
        setAdapter()
    }

    private fun setButtonFunction() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setAdapter() {
        user = pref.values.loggedUser!!
        adapter = PreferenceAdapter(this)
        binding.rvPreferences.adapter = adapter
        val layoutManager = FlexboxLayoutManager(this)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.CENTER
        layoutManager.alignItems = AlignItems.CENTER
        layoutManager.flexWrap = FlexWrap.WRAP
        binding.rvPreferences.layoutManager = layoutManager
        val categories = getCheckCategories(preferencesViewModel.getAllCategories())
        adapter.submitList(categories, user.preferences)
        adapter.setOnItemClickListener {
            updatePreferences(it)
        }
    }

    private fun updatePreferences(item: CircularProgressButton) {
        binding.txtError.isVisible = false
        if (adapter.getItemSelected() < 3) {
            Toast.makeText(this@PreferencesActivity, "Es necesario agregar almenos 3 preferencias.", Toast.LENGTH_LONG).show()
            return
        }

        item.startAnimation {
            item.background = getDrawableCompat(R.drawable.custom_btn)
        }

        val user = pref.values.loggedUser!!
        val jsonPref = adapter.getJsonList()
        val userUpdate = UserRequest(user.name, user.lastname, "", user.email, user.address, user.phone, user.token, jsonPref, user.id)
        lifecycleScope.launch {
            val result = loginViewModel.updateUser(userUpdate)
            val response = result.getOrNull()
            item.revertAnimation()
            if (response.isNull()) {
                Toast.makeText(this@PreferencesActivity, "Error al conectarse con el servidor.", Toast.LENGTH_LONG).show()
                return@launch
            }
            onUpdate(response!!)
        }

    }

    private fun onUpdate(resp: LoginResponse) {
        if (resp.error.isNotNull()) {
            binding.txtError.isVisible = true
            binding.txtError.text = resp.error
            return
        }
        pref.values.loggedUser = resp.user
        pref.update()
        Toast.makeText(this, "Se actualizo correctamente", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun getCheckCategories(list: List<Category>): List<CategoryCheck> {
        val categories = mutableListOf<CategoryCheck>()
        list.forEach {
            categories.add(CategoryCheck(0, 0, it.name, it.description, false, true, "", ""))
            categories.addAll(it.subCategories.map { sub -> CategoryCheck(it.id, sub.id, it.name, it.description, false, false, sub.name, sub.description) })
        }

        categories.add(CategoryCheck(0, 0, "", "", false, true, "", ""))

        return categories
    }

}