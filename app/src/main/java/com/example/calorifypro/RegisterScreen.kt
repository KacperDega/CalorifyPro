package com.example.calorifypro

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RegisterScreen : Fragment() {

    private lateinit var registerButton: Button
    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var errorTextView: TextView
    private lateinit var hidePasswordToggle : ImageButton
    private lateinit var passwordConfirmInput : EditText
    private lateinit var hidePasswordConfirmToggle : ImageButton

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        registerButton = view.findViewById(R.id.buttonRegister)
        usernameInput = view.findViewById(R.id.usernameInput)
        emailInput = view.findViewById(R.id.emailInput)
        passwordInput = view.findViewById(R.id.passwordInput)
        errorTextView = view.findViewById(R.id.errorTextView)
        hidePasswordToggle = view.findViewById(R.id.hidePasswordToggle)
        passwordConfirmInput = view.findViewById(R.id.passwordConfirmInput)
        hidePasswordConfirmToggle = view.findViewById(R.id.hidePasswordConfirmToggle)

        firestore = FirebaseFirestore.getInstance()

        hidePasswordToggle.setOnClickListener()
        {
            toggleHidePassword(passwordInput, hidePasswordToggle)
        }

        hidePasswordConfirmToggle.setOnClickListener()
        {
            toggleHidePassword(passwordConfirmInput, hidePasswordConfirmToggle)
        }

        registerButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val email = emailInput.text.toString().lowercase()
            val password = passwordInput.text.toString()
            val confirmPassword = passwordConfirmInput.text.toString()

            // czy pola nie sa puste
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                errorTextView.text = "All boxes must be filled"
                return@setOnClickListener
            }

            // czy e-mail jest w formacie e-maila
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errorTextView.text = "Incorrect E-mail format"
                return@setOnClickListener
            }

            // czy hasła się zgadzaja (nie pytam ich o zgode ;~) )
            if (password != confirmPassword) {
                errorTextView.text = "Passwords are not matching"
                return@setOnClickListener
            }


            // czy uzytkownik już istnieje
            firestore.collection("users")
                .document(email)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null && document.exists()) {
                            // no istnieje
                            errorTextView.text = "Email already in use"
                        } else {
                            // nie istnieje dodajemy
                            val newUser = hashMapOf(
                                "avatar" to "default_avatar",
                                "email" to email,
                                "joinDate" to FieldValue.serverTimestamp(),
                                "password" to password,
                                "username" to username
                            )

                            // Dodawanie uzytkownika do users
                            firestore.collection("users")
                                .document(email)
                                .set(newUser)
                                .addOnSuccessListener {
                                // Dodano uzytkownika
                                    firestore.collection("users")
                                        .document(email)
                                        .collection("days")
                                        .document(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                                        .set(hashMapOf("tempField" to "tempValue"))  // tymczasowe pole
                                        .addOnSuccessListener {
                                            // Nie da sie dodac pustego pola tylko trzeba dodać jakieś i je usunąć (?)
                                            firestore.collection("users")
                                                .document(email)
                                                .collection("days")
                                                .document(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                                                .update(hashMapOf("tempField" to FieldValue.delete()) as Map<String, Any>)
                                                .addOnSuccessListener {

                                                    // Konto utworzone
                                                    Toast.makeText(requireContext(), "Account created successfully", Toast.LENGTH_SHORT).show()

                                                    // Przed zalogowaniem zapisac trzeba email do sharedpreferences
                                                    val sharedPreferencesManager = SharedPreferencesManager(requireContext())
                                                    sharedPreferencesManager.userEmail = email

                                                    // Przechodzenie do aktywności
                                                    val intent = Intent(requireContext(), AppActivity::class.java)
                                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    startActivity(intent)
                                                }
                                                .addOnFailureListener {
                                                    // Błąd podczas usuwania tymczasowego pola
                                                    errorTextView.text = "Error creating account: 1"
                                                }
                                        }
                                        .addOnFailureListener {
                                            // Błąd podczas tworzenia podkolekcji days
                                            errorTextView.text = "Error creating account: 2"
                                        }

                                }
                                .addOnFailureListener {
                                    // Błąd podczas dodawania użytkownika do kolekcji users
                                    errorTextView.text = "Error creating account: 3"
                                }
                        }
                    } else {
                        // Błąd podczas sprawdzania użytkownika
                        errorTextView.text = "Error creating account: 4"
                    }
                }
        }

        return view
    }

    private fun toggleHidePassword(passwordBox : EditText, eyeButton : ImageButton)
    {
        val typeface = passwordBox.typeface

        passwordBox.inputType =
            if (passwordBox.transformationMethod == PasswordTransformationMethod.getInstance()) {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

        passwordBox.typeface = typeface

        val visibilityIcon =
            if (passwordBox.transformationMethod == PasswordTransformationMethod.getInstance()) {
                R.drawable.ic_eye_off_outline
            } else {
                R.drawable.ic_eye_outline
            }

        eyeButton.setImageResource(visibilityIcon)

        passwordBox.setSelection(passwordBox.text.length)
    }
}
