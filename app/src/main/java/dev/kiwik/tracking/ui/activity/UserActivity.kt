package dev.kiwik.tracking.ui.activity

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dev.kiwik.tracking.R
import dev.kiwik.tracking.databinding.ActivityUserBinding
import dev.kiwik.tracking.domain.api.LoginResponse
import dev.kiwik.tracking.domain.entities.User
import dev.kiwik.tracking.domain.entities.UserRequest
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.utilities.getDrawableCompat
import dev.kiwik.tracking.utilities.isNotNull
import dev.kiwik.tracking.utilities.isNull
import dev.kiwik.tracking.viewmodel.LoginViewModel
import kotlinx.coroutines.launch

class UserActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUserBinding
    private lateinit var user : User
    private lateinit var userUpdate: UserRequest

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.color_primary)
        setButtonFunctions()
        setData()
    }

    private fun setData(){
        val pref = Pref.getInstance()
        user = pref.values.loggedUser!!
        binding.editName.setText(user.name)
        binding.editLastName.setText(user.lastname)
        binding.editAddress.setText(user.address)
        binding.editPhone.setText(user.phone)
    }


    private fun validateForm(): Boolean {
        val name = binding.editName.text.toString()
        val surname = binding.editLastName.text.toString()
        val address = binding.editAddress.text.toString()
        val phone = binding.editPhone.text.toString()
        userUpdate = UserRequest(name,surname,"","",address,phone, userId = user.id, preferences = user.preferences)
        return name.isNotBlank() and surname.isNotBlank() and address.isNotBlank() and phone.isNotBlank()
    }

    private fun setButtonFunctions(){
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnUpdate.setOnClickListener {
            updateInfo()
        }
    }

    private fun updateInfo(){
        if (!validateForm()) {
            binding.txtError.isVisible = true
            binding.txtError.text = "Es necesario llenar todos los campos."
        }

        binding.btnUpdate.startAnimation{
            binding.btnUpdate.background = getDrawableCompat(R.drawable.custom_btn)
        }

        lifecycleScope.launch {
            val result = loginViewModel.updateUser(userUpdate)
            val response = result.getOrNull()
            binding.btnUpdate.revertAnimation()
            if (response.isNull()) {
                binding.txtError.text = "Error al conectarse con el servidor."
                binding.txtError.isVisible = true
                return@launch
            }
            onUpdate(response!!)
        }
    }

    private fun onUpdate(response: LoginResponse){
        if(response.error.isNotNull()){
            binding.txtError.isVisible = true
            binding.txtError.text = response.error
            return
        }
        val pref = Pref.getInstance()
        pref.values.loggedUser = response.user
        pref.update()

        Toast.makeText(this, "Se actualizo correctamente", Toast.LENGTH_LONG).show()
        setData()
    }
}