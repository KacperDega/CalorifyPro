package com.example.calorifypro

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.example.calorifypro.SharedPreferencesManager

class LoginScreen : Fragment() {

    private lateinit var loginButton: Button
    private lateinit var emailInput : EditText
    private lateinit var passwordInput : EditText
    private lateinit var errorTextView : TextView
    private lateinit var hidePasswordToggle : ImageView

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Inicjalizacje
        loginButton = view.findViewById(R.id.buttonLogin)
        emailInput = view.findViewById(R.id.loginInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        errorTextView = view.findViewById(R.id.errorTextView)
        hidePasswordToggle = view.findViewById(R.id.hidePasswordToggle)

        firestore = FirebaseFirestore.getInstance()

        hidePasswordToggle.setOnClickListener()
        {
            toggleHidePassword()
        }


        loginButton.setOnClickListener {
            val email = emailInput.text.toString().lowercase()
            val password = passwordInput.text.toString()

            // Sprawdzenie czy pola nie sa puste
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "All boxes must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sprawdzenie czy e-mail jest w formacie e-maila
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Incorrect E-mail format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sprawdzenie czy użytkownik istnieje w bazie
            firestore.collection("users")
                .document(email)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {

                            // Użytkownik istnieje w bazie
                            // Sprawdz czy haslo się zgadza
                            val correctPassword = document.getString("password")

                            if (password == correctPassword) {

                                // Przed zalogowaniem zapisac trzeba email do sharedpreferences
                                val sharedPreferencesManager = SharedPreferencesManager(requireContext())
                                sharedPreferencesManager.userEmail = email

                                // Haslo sie zgadza, zaloguj
                                val intent = Intent(requireContext(), AppActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            } else {
                                // Haslo niepoprawne
                                errorTextView.text = "Incorrect password"
                            }
                        } else {
                            // Uzytkownik nie istnieje w bazie
                            errorTextView.text = "User doesn't exist"
                        }
                    } else {
                        // Blad podczas pobierania danych z bazy
                        errorTextView.text = "Checking user error."
                    }
                }
        }

        return view
    }

    //toggle pokazywania hasla
    private fun toggleHidePassword()
    {
        val typeface = passwordInput.typeface

        passwordInput.inputType =
            if (passwordInput.transformationMethod == PasswordTransformationMethod.getInstance()) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

        passwordInput.typeface = typeface

        val visibilityIcon =
            if (passwordInput.transformationMethod == PasswordTransformationMethod.getInstance()) {
                R.drawable.ic_eye_off_outline
            } else {
                R.drawable.ic_eye_outline
            }

        hidePasswordToggle.setImageResource(visibilityIcon)

        passwordInput.setSelection(passwordInput.text.length)
    }
}

