package dev.kiwik.tracking.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dev.kiwik.tracking.R
import dev.kiwik.tracking.databinding.ActivityLoginBinding
import dev.kiwik.tracking.domain.api.LoginResponse
import dev.kiwik.tracking.domain.db.AppDatabase
import dev.kiwik.tracking.domain.entities.User
import dev.kiwik.tracking.preferences.Pref
import dev.kiwik.tracking.utilities.checkAndRequestPermission
import dev.kiwik.tracking.utilities.getDrawableCompat
import dev.kiwik.tracking.utilities.isNull
import dev.kiwik.tracking.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.color_primary)
        checkAndRequestPermission(3000, *permissions)
        setButtonFunction()
        initGoogleAuth()
    }

    private fun initGoogleAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setButtonFunction() {
        binding.btnLogin.setOnClickListener {
            if (!formValidate()) return@setOnClickListener
            login()
        }
        binding.btnRegister.setOnClickListener {
            val pref = Pref.getInstance()
            pref.values.userToAdd = null
            pref.values.intentRegister = 0
            pref.update()
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnRegisterGoogle.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        binding.txtError.isVisible = false
        startActivityForResult(signInIntent, 321)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 321) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val auth = Firebase.auth
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signOut()
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val pref = Pref.getInstance()
                        lifecycleScope.launch {
                            val result = loginViewModel.loginWithGoogle(user?.email
                                    ?: "", user?.phoneNumber ?: "", user?.displayName
                                    ?: "")
                            val response = result.getOrNull()
                            if (response.isNull()) {
                                binding.txtError.text = "Error al conectarse con el servidor."
                                binding.txtError.isVisible = true
                                return@launch
                            }
                            onLogin(response!!)
                        }
                    } else {
                        binding.txtError.isVisible = true
                        binding.txtError.text = "No se pudo registrar con Google"
                    }
                }
    }

    private fun login() {
        binding.btnLogin.startAnimation {
            binding.btnLogin.background = getDrawableCompat(R.drawable.custom_btn)
        }
        val email = binding.editUser.text.toString()
        val password = binding.editPassword.text.toString()
        lifecycleScope.launch {
            val result = loginViewModel.login(email, password)
            val response = result.getOrNull()
            binding.btnLogin.revertAnimation()
            if (response.isNull()) {
                binding.txtError.text = "Error al conectarse con el servidor."
                binding.txtError.isVisible = true
                return@launch
            }
            onLogin(response!!)
        }
    }

    private fun onLogin(response: LoginResponse) {
        if (response.error != null) {
            binding.txtError.text = response.error
            binding.txtError.isVisible = true
            return
        }
        binding.txtError.isVisible = false
        val pref = Pref.getInstance()
        pref.values.loggedUser = response.user
        pref.update()
        val bd = AppDatabase.getInstance()
        runBlocking {
            bd.trackingDao().truncate()
            bd.resourceDao().truncateCategories()
            bd.resourceDao().truncatePlaces()
            bd.trackingBaseDao().truncate()
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()

    }

    private val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_SMS,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.VIBRATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED
    )

    private fun formValidate(): Boolean {
        val isValidate = !binding.editUser.text.isBlank() and !binding.editPassword.text.isBlank()
        if (!isValidate) {
            Toast.makeText(this, "Es necesario agregar todos los datos.", Toast.LENGTH_LONG).show()
        }
        return isValidate
    }

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
}