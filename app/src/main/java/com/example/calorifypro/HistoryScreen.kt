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

class HistoryScreen : Fragment() {

    private lateinit var recyclerViewHistory: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var firestore: FirebaseFirestore
    private lateinit var email: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerViewHistory = view.findViewById(R.id.recyclerViewHistory)

        firestore = FirebaseFirestore.getInstance()

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        email = sharedPreferencesManager.userEmail!!

        // Pobieramy daty z kolekcji "days" użytkownika
        getDatesFromFirestore()

        return view
    }


    private fun getDatesFromFirestore() {
        firestore.collection("users")
            .document(email)
            .collection("days")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val dateList = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    dateList.add(document.id)
                }

                setupRecyclerView(dateList)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching dates.", Toast.LENGTH_LONG).show()
            }
    }

    private fun setupRecyclerView(dateList: List<String>) {
        historyAdapter = HistoryAdapter(dateList, object : HistoryAdapter.OnItemClickListener {
            override fun onItemClick(date: String) {
                goToDay(date)
            }

            override fun onItemButtonClick(date: String) {
                goToDay(date)
            }
        })

        recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewHistory.adapter = historyAdapter
    }

    private fun goToDay(date : String){
        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        sharedPreferencesManager.selectedDate = date


        val fragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(
            R.anim.slide_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.slide_out
        )
        fragmentTransaction.replace(R.id.fragment_container_app, HistoryDayScreen())
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}