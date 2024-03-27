package com.piwew.myreactiveform

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding2.widget.RxTextView
import com.piwew.myreactiveform.databinding.ActivityMainBinding
import io.reactivex.Observable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val emailStream = RxTextView.textChanges(binding.edEmail)
            .skipInitialValue()
            .map { email -> !Patterns.EMAIL_ADDRESS.matcher(email).matches() }
        emailStream.subscribe { showEmailExistAlert(it) }

        val passwordStream = RxTextView.textChanges(binding.edPassword)
            .skipInitialValue()
            .map { password -> password.length < 6 }
        passwordStream.subscribe { showPasswordMinimalAlert(it)}

        val passwordConfirmationStream = Observable.merge(
            RxTextView.textChanges(binding.edPassword)
                .map { password -> password.toString() != binding.edConfirmPassword.text.toString() },
            RxTextView.textChanges(binding.edConfirmPassword)
                .map { confirmPassword -> confirmPassword.toString() != binding.edPassword.text.toString() }
        )
        passwordConfirmationStream.subscribe { showPasswordConfirmationAlert(it) }

        val invalidFieldsStream = Observable.combineLatest(
            emailStream,
            passwordStream,
            passwordConfirmationStream
        ) { emailInvalid: Boolean, passwordInvalid: Boolean, passwordConfirmationInvalid: Boolean ->
            !emailInvalid && !passwordInvalid && !passwordConfirmationInvalid
        }
        invalidFieldsStream.subscribe { isValid ->
            binding.apply {
                if (isValid) {
                    btnRegister.isEnabled = true
                    btnRegister.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.purple_500))
                } else {
                    btnRegister.isEnabled = false
                    btnRegister.setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.darker_gray))
                }
            }
        }
    }

    private fun showEmailExistAlert(isNotValid: Boolean) {
        binding.edEmail.error = if (isNotValid) getString(R.string.email_not_valid) else null
    }

    private fun showPasswordMinimalAlert(isNotValid: Boolean) {
        binding.edPassword.error = if (isNotValid) getString(R.string.password_not_valid) else null
    }

    private fun showPasswordConfirmationAlert(isNotValid: Boolean) {
        binding.edConfirmPassword.error = if (isNotValid) getString(R.string.password_not_same) else null
    }
}