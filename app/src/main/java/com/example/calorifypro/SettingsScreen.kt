package com.example.calorifypro

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class SettingsScreen : Fragment() {

    private lateinit var buttonChangeAvatar : Button
    private lateinit var editTextUsername : EditText
    private lateinit var buttonChangeUsername : Button
    private lateinit var editTextPassword : EditText
    private lateinit var editTextConfirmPassword : EditText
    private lateinit var buttonChangePassword : Button

    private lateinit var avatar0 : ImageView
    private lateinit var avatar1 : ImageView
    private lateinit var avatar2 : ImageView
    private lateinit var avatar3 : ImageView
    private lateinit var avatar4 : ImageView
    private lateinit var avatar5 : ImageView
    private lateinit var avatar6 : ImageView
    private lateinit var avatar7 : ImageView
    private lateinit var avatar8 : ImageView
    private var selectedAvatar: ImageView? = null

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        buttonChangeAvatar = view.findViewById(R.id.buttonChangeAvatar)
        editTextUsername = view.findViewById(R.id.editTextUsername)
        buttonChangeUsername = view.findViewById(R.id.buttonChangeUsername)
        editTextPassword = view.findViewById(R.id.editTextPassword)
        editTextConfirmPassword = view.findViewById(R.id.editTextConfirmPassword)
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword)

        firestore = FirebaseFirestore.getInstance()

        //pobranie email z pamieci
        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        var email = sharedPreferencesManager.userEmail

        val activity = requireActivity() as AppCompatActivity
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)

        // Ustaw obsługę kliknięcia przycisku wstecz
        toolbar.setNavigationOnClickListener {
            activity.onBackPressed()
        }

        //zmiana username
        buttonChangeUsername.setOnClickListener()
        {
            val newUsername = editTextUsername.text.toString()

            if (newUsername.isEmpty()){
                Toast.makeText(requireContext(),"New username cannot be empty.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            //pobieranie danych uzytkownika
            val userReference = firestore.collection("users").document(email!!)

            // Update nazwę użytkownika
            userReference.update("username", newUsername)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Username updated successfully.", Toast.LENGTH_SHORT).show()
                    editTextUsername.text.clear()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error updating username: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        //zmiana hasla
        buttonChangePassword.setOnClickListener()
        {
            val newPassword = editTextPassword.text.toString()
            val newPasswordConfirm = editTextConfirmPassword.text.toString()

            if (newPassword.isEmpty() || newPasswordConfirm.isEmpty())
            {
                Toast.makeText(requireContext(),"Password fields cannot be empty.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (newPassword != newPasswordConfirm)
            {
                Toast.makeText(requireContext(),"Passwords are different.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Pobieranie danych użytkownika
            val userReference = firestore.collection("users").document(email!!)

            // Uaktualnij hasło użytkownika
            userReference.update("password", newPassword)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Password updated successfully.", Toast.LENGTH_SHORT).show()
                    editTextPassword.text.clear()
                    editTextConfirmPassword.text.clear()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Error updating password: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        //AVATARY
        avatar0 = view.findViewById(R.id.avatar0);
        avatar1 = view.findViewById(R.id.avatar1);
        avatar2 = view.findViewById(R.id.avatar2);
        avatar3 = view.findViewById(R.id.avatar3);
        avatar4 = view.findViewById(R.id.avatar4);
        avatar5 = view.findViewById(R.id.avatar5);
        avatar6 = view.findViewById(R.id.avatar6);
        avatar7 = view.findViewById(R.id.avatar7);
        avatar8 = view.findViewById(R.id.avatar8);

        avatar0.setOnClickListener { v -> onAvatarClick(v) }
        avatar1.setOnClickListener { v -> onAvatarClick(v) }
        avatar2.setOnClickListener { v -> onAvatarClick(v) }
        avatar3.setOnClickListener { v -> onAvatarClick(v) }
        avatar4.setOnClickListener { v -> onAvatarClick(v) }
        avatar5.setOnClickListener { v -> onAvatarClick(v) }
        avatar6.setOnClickListener { v -> onAvatarClick(v) }
        avatar7.setOnClickListener { v -> onAvatarClick(v) }
        avatar8.setOnClickListener { v -> onAvatarClick(v) }

        buttonChangeAvatar.setOnClickListener {
            val fragmentView = view

            fragmentView?.let { view ->
                val selectedAvatarId: Int = selectedAvatar?.id ?: 0

                var avatarName : String? = null

                when (selectedAvatarId) {
                    view.findViewById<ImageView>(R.id.avatar0)?.id -> {
                        avatarName = "default_avatar"
                    }
                    view.findViewById<ImageView>(R.id.avatar1)?.id -> {
                        avatarName = "man_1"
                    }
                    view.findViewById<ImageView>(R.id.avatar2)?.id -> {
                        avatarName = "man_2"
                    }
                    view.findViewById<ImageView>(R.id.avatar3)?.id -> {
                        avatarName = "man_3"
                    }
                    view.findViewById<ImageView>(R.id.avatar4)?.id -> {
                        avatarName = "man_4"
                    }
                    view.findViewById<ImageView>(R.id.avatar5)?.id -> {
                        avatarName = "woman_1"
                    }
                    view.findViewById<ImageView>(R.id.avatar6)?.id -> {
                        avatarName = "woman_2"
                    }
                    view.findViewById<ImageView>(R.id.avatar7)?.id -> {
                        avatarName = "woman_3"
                    }
                    view.findViewById<ImageView>(R.id.avatar8)?.id -> {
                        avatarName = "woman_4"
                    }
                }

                if (avatarName.isNullOrEmpty()){
                    Toast.makeText(requireContext(), "No avatar selected", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val userReference = firestore.collection("users").document(email!!)

                // Zaktualizuj avatar użytkownika
                userReference.update("avatar", avatarName)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Avatar updated successfully.", Toast.LENGTH_SHORT).show()
                        //Odznacz avatar
                        selectedAvatar?.apply {
                            isSelected = false
                            setBackgroundColor(Color.TRANSPARENT)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(requireContext(), "Error updating avatar: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }

            } ?: run {
                Toast.makeText(requireContext(), "No avatar selected", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }


    fun onAvatarClick(v: View) {
        val clickedAvatar: ImageView = v as ImageView

        // Czy zaznaczony jakiś jest
        selectedAvatar?.let {
            // Odznacz i przywróć kolor tła
            it.isSelected = false
            it.setBackgroundColor(Color.TRANSPARENT)
        }

        // Zaznaczanie nowego
        clickedAvatar.isSelected = true
        //clickedAvatar.setBackgroundColor(Color.parseColor("#C79098"))
        clickedAvatar.setBackgroundResource(R.drawable.list_button_rounded)
        selectedAvatar = clickedAvatar
    }

}

