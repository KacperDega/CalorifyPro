package com.example.calorifypro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.FirebaseApp

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Inicjalizacja Firebase
        FirebaseApp.initializeApp(this)

        if (savedInstanceState == null) {

            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container_welcome, WelcomeScreen())
            fragmentTransaction.commit()
        }
    }
}