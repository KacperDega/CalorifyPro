package com.example.calorifypro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class WelcomeScreen : Fragment() {

    private lateinit var toLoginButton: Button
    private lateinit var toRegisterButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_welcome_screen, container, false)

        toLoginButton = view.findViewById(R.id.buttonLogin)
        toRegisterButton = view.findViewById(R.id.buttonRegister)

        // Log In
        toLoginButton.setOnClickListener {
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container_welcome, LoginScreen())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        // Register
        toRegisterButton.setOnClickListener {
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container_welcome, RegisterScreen())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        return view
    }
}
