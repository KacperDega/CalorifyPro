package com.example.calorifypro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LeaderboardScreen : Fragment() {

    private lateinit var recyclerViewLeaderboard : RecyclerView

    private lateinit var firestore: FirebaseFirestore
    lateinit var email : String

    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_leaderboard, container, false)

        recyclerViewLeaderboard = view.findViewById(R.id.recycler_view_leaderboard)

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        email = sharedPreferencesManager.userEmail!!

        firestore = FirebaseFirestore.getInstance()


        leaderboardAdapter = LeaderboardAdapter(requireContext(), mutableListOf())
        recyclerViewLeaderboard.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewLeaderboard.adapter = leaderboardAdapter

        getLeaderboardData()
        getTop10Leaderboard()

        return view
    }

    private fun getLeaderboardData() {
        val formatterDB = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        //val currentDate = LocalDateTime.now().format(formatterDB)
        val previousDate = LocalDateTime.now().minusDays(1).format(formatterDB)

        firestore.collection("users")
            .document(email)
            .collection("days")
            .document(previousDate)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Dokument istnieje, pobierz dane posiłków
                    val meals = document.data?.get("meals") as List<Map<String, Any>>?
                    if (meals != null) {
                        // Policzenie sumy spożytych kalorii w dniu poprzednim
                        val consumedCalories = meals.sumOf { it["calories"].toString().toDouble() }

                        // Pobranie celu kalorii użytkownika
                        val targetCalories = document.getDouble("target") ?: 0.0

                        // Obliczenie różnicy między spożytymi a celem
                        var difference = Math.abs(targetCalories - consumedCalories)
                        difference = BigDecimal(difference).setScale(2, RoundingMode.HALF_EVEN).toDouble()

                        // Zaktualizowanie rankingu
                        updateLeaderboard(email, difference)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error getting data from server.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateLeaderboard(email: String, difference: Double) {
        firestore.collection("leaderboard")
            .document(email)
            .set(mapOf("difference" to difference))
            .addOnSuccessListener {
                getTop10Leaderboard()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error updating leaderboard.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun getTop10Leaderboard() {
        firestore.collection("leaderboard")
            .orderBy("difference")
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                if (result != null) {
                    val userList = mutableListOf<LeaderboardAdapter.User>()

                    for (document in result) {
                        val userEmail = document.id
                        val difference = document.getDouble("difference").toString()

                        // Dodatkowe zapytanie do pobrania danych użytkownika
                        firestore.collection("users")
                            .document(userEmail)
                            .get()
                            .addOnSuccessListener { userDocument ->
                                val avatar = userDocument.getString("avatar") ?: "default_avatar"
                                val username = userDocument.getString("username") ?: "UnknownUsername"

                                val user = LeaderboardAdapter.User(
                                    rank = "",  // Wypełnij rank po sortowaniu
                                    username = username,
                                    avatar = avatar,
                                    value = difference
                                )
                                userList.add(user)

                                // Sprawdź, czy uzyskano już dane dla wszystkich użytkowników
                                if (userList.size == result.documents.size) {
                                    // Posortuj listę na podstawie wartości (różnicy kalorii)
                                    userList.sortBy { it.value.toDouble() }

                                    // Przydziel rangi
                                    for ((index, user) in userList.withIndex()) {
                                        user.rank = (index + 1).toString()
                                    }

                                    leaderboardAdapter.setUsers(userList)
                                }
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(requireContext(), "Error getting user data.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error getting leaderboard.", Toast.LENGTH_SHORT).show()
            }
    }


}
