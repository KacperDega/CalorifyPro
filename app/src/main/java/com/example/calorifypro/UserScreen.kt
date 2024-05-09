package com.example.calorifypro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class UserScreen : Fragment() {

    private lateinit var settingsButton : ImageButton
    private lateinit var avatarImageView : ImageView
    private lateinit var usernameLargeText : TextView
    private lateinit var joinDateText : TextView
    private lateinit var emailTextView : TextView
    private lateinit var usernameTextView: TextView
    private lateinit var logoutButton : Button

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user, container, false)

        settingsButton = view.findViewById(R.id.settingsIcon)
        avatarImageView = view.findViewById(R.id.avatarImageView)
        usernameLargeText = view.findViewById(R.id.usernameLargeText)
        joinDateText = view.findViewById(R.id.joinDateText)
        emailTextView = view.findViewById(R.id.emailTextView)
        usernameTextView = view.findViewById(R.id.usernameTextView)
        logoutButton = view.findViewById(R.id.logoutButton)

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        firestore = FirebaseFirestore.getInstance()

        var email = sharedPreferencesManager.userEmail

        //pobieranie danych uzytkownika
        val userReference = firestore.collection("users").document(email!!)

        userReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val avatar = documentSnapshot.getString("avatar")
                    val joinDate = documentSnapshot.getTimestamp("joinDate")
                    val username = documentSnapshot.getString("username")

                    val avatarID = resources.getIdentifier(avatar, "drawable", requireActivity().packageName)
                    avatarImageView.setImageResource(avatarID)

                    usernameLargeText.text = username

                    val temp = joinDate?.toDate()
                    val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH)
                    val formattedDate = dateFormat.format(temp)
                    joinDateText.text = "Joined: " + formattedDate

                    emailTextView.text = email
                    usernameTextView.text = username
                }
            }
            .addOnFailureListener { exception ->
                    usernameLargeText.text = "Error geting userdata from server"
            }


        // Settings Button
        settingsButton.setOnClickListener {
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            fragmentTransaction.replace(R.id.fragment_container_app, SettingsScreen())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        logoutButton.setOnClickListener {
            // Przed wylogowaniem trzeba wyczyscic sharedpreferences
            sharedPreferencesManager.clearUserData()

            val intent = Intent(requireContext(), StartActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}