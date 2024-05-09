package com.example.calorifypro

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppActivity : AppCompatActivity() {

    private lateinit var bottomNavView: BottomNavigationView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var email : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        //Gdzies trzeba wpisywac dane to tworzymy dokument na dzisiaj cnie
        val sharedPreferencesManager = SharedPreferencesManager(this)
        email = sharedPreferencesManager.userEmail!!

        firestore = FirebaseFirestore.getInstance()
        checkDocumentForToday()

        if (savedInstanceState == null) {

            val fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()

            fragmentTransaction.setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )

            fragmentTransaction.replace(R.id.fragment_container_app, HomeScreen())
            fragmentTransaction.commit()
        }


        bottomNavView = findViewById(R.id.bottom_nav_bar)
        bottomNavView.selectedItemId = R.id.menu_home

        // Listener dla kliknięcia na element menu
        bottomNavView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                //home
                R.id.menu_home -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                    )
                    fragmentTransaction.replace(R.id.fragment_container_app, HomeScreen())
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                    true
                }
                //user
                R.id.menu_user -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                    )
                    fragmentTransaction.replace(R.id.fragment_container_app, UserScreen())
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                    true
                }
                R.id.menu_leaderboard -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                    )
                    fragmentTransaction.replace(R.id.fragment_container_app, LeaderboardScreen())
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                    true
                }
                R.id.menu_history -> {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.setCustomAnimations(
                        R.anim.slide_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.slide_out
                    )
                    fragmentTransaction.replace(R.id.fragment_container_app, HistoryScreen())
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkDocumentForToday() {
        // Formatowanie dzisiejszej daty zeby miec nazwe dokumentu
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val todayDate = dateFormat.format(Calendar.getInstance().time)

        val documentReference = firestore.collection("users").document(email)
            .collection("days").document(todayDate)

        // Sprawdzenie, czy dokument juz istnieje
        documentReference.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentSnapshot = task.result
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Dokument już istnieje, czilera
                        // W razie czego sprawdzimy pola
                        val updateMap = mutableMapOf<String, Any>()

                        if (!documentSnapshot.contains("target")) {
                            updateMap["target"] = 2000
                        }

                        if (!documentSnapshot.contains("meals")) {
                            val mealsData = emptyList<Map<String, Any>>()
                            updateMap["meals"] = mealsData
                        }

                        if (updateMap.isNotEmpty()) {
                            documentReference.update(updateMap)
                        }
                    } else {
                        // Dokument nie istnieje, tworzymy, siup
                        val mealsData = emptyList<Map<String, Any>>()
                        val targetData = 2000
                        val data = hashMapOf("meals" to mealsData, "target" to targetData)
                        documentReference.set(data)
                    }
                }
            }
    }
}