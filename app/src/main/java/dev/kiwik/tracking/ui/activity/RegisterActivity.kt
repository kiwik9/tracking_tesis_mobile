package dev.kiwik.tracking.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dev.kiwik.tracking.R
import dev.kiwik.tracking.databinding.ActivityRegisterBinding
import dev.kiwik.tracking.domain.api.LoginResponse
import dev.kiwik.tracking.domain.entities.UserRequest
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.utilities.getDrawableCompat
import dev.kiwik.tracking.utilities.isNotNull
import dev.kiwik.tracking.utilities.isNull
import dev.kiwik.tracking.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userRegister: UserRequest
    private val loginViewModel: LoginViewModel by viewModels()
    private val pref by lazy {
        Pref.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (pref.values.loggedUser.isNotNull()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.color_primary)

        setButtonFunctions()
        setData()
    }

    private fun setButtonFunctions() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnRegister.setOnClickListener {
            register()
        }
    }

    private fun setData(){
        if(pref.values.intentRegister == 743 && pref.values.userToAdd.isNotNull()){
            val user = pref.values.userToAdd!!
            binding.editName.setText(user.name)
            binding.editUser.setText(user.email)
            binding.editPhone.setText(user.phone)
        }
    }
    private fun validateForm(): Boolean {
        val email = binding.editUser.text.toString()
        val password = binding.editPassword.text.toString()
        val name = binding.editName.text.toString()
        val surname = binding.editLastName.text.toString()
        val address = binding.editAddress.text.toString()
        val phone = binding.editPhone.text.toString()
        userRegister = UserRequest(name, surname, password, email, address, phone)
        return email.isNotBlank() and password.isNotBlank() and name.isNotBlank() and
                surname.isNotBlank() and address.isNotBlank() and phone.isNotBlank()
    }

    @SuppressLint("SetTextI18n")
    private fun register() {
        if (!validateForm()) {
            binding.txtError.isVisible = true
            binding.txtError.text = "Es necesario llenar todos los campos."
            return
        }
        binding.btnRegister.startAnimation{
            binding.btnRegister.background = getDrawableCompat(R.drawable.custom_btn)
        }

        lifecycleScope.launch {
            val result = loginViewModel.register(userRegister)
            val response = result.getOrNull()
            binding.btnRegister.revertAnimation()
            if (response.isNull()) {
                binding.txtError.text = "Error al conectarse con el servidor."
                binding.txtError.isVisible = true
                return@launch
            }
            onRegister(response!!)
        }
    }

    private fun onRegister(response: LoginResponse) {
        if(response.error.isNotNull()){
            binding.txtError.isVisible = true
            binding.txtError.text = response.error
            return
        }
        pref.values.loggedUser = response.user
        pref.update()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }
}