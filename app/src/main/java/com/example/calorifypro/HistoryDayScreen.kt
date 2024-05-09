package com.example.calorifypro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HistoryDayScreen : Fragment(){

    private lateinit var toolbar : Toolbar
    private lateinit var toolbarText : TextView
    private lateinit var recyclerViewDay : RecyclerView
    private lateinit var targetTextView: TextView
    private lateinit var caloriesTextView: TextView
    private lateinit var fatTextView: TextView
    private lateinit var carbsTextView: TextView

    private lateinit var firestore: FirebaseFirestore
    lateinit var email : String
    lateinit var selectedDate : String

    private lateinit var mealListAdapter: MealListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history_day, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        toolbarText = view.findViewById(R.id.toolbarText)
        recyclerViewDay = view.findViewById(R.id.recyclerViewDay)
        targetTextView = view.findViewById(R.id.targetTextView)
        caloriesTextView = view.findViewById(R.id.caloriesTextView)
        fatTextView = view.findViewById(R.id.fatTextView)
        carbsTextView = view.findViewById(R.id.carbsTextView)

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        email = sharedPreferencesManager.userEmail!!
        selectedDate = sharedPreferencesManager.selectedDate!!

        toolbarText.text = selectedDate

        firestore = FirebaseFirestore.getInstance()

        // back button na pasku gornym
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        //pobranie posilkow z bazy
        mealListAdapter = MealListAdapter(false)
        recyclerViewDay.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewDay.adapter = mealListAdapter
        getMealsFromFirestore()

        return view
    }

    private fun getMealsFromFirestore() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val documentName = dateFormat.format(Calendar.getInstance().time)

        firestore.collection("users")
            .document(email)
            .collection("days")
            .document(selectedDate)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val meals = document.data?.get("meals") as List<Map<String, Any>>?
                    val target = document.data?.get("target") as Long?

                    if (meals != null) {
                        val mealList = mutableListOf<Map<String, Any>>()
                        var totalCalories = 0.0
                        var totalFat = 0.0
                        var totalCarbohydrates = 0.0

                        for (meal in meals) {
                            mealList.add(meal)

                            val calories = meal["calories"] as? Double ?: 0.0
                            val fat = meal["fat"] as? Double ?: 0.0
                            val carbohydrates = meal["carbohydrates"] as? Double ?: 0.0

                            totalCalories += calories
                            totalFat += fat
                            totalCarbohydrates += carbohydrates
                        }

                        mealListAdapter.setMeals(mealList)

                        val decimalFormat = DecimalFormat("#.##")

                        caloriesTextView.text = "Cal.: ${decimalFormat.format(totalCalories)}kcal"
                        fatTextView.text = "Fat: ${decimalFormat.format(totalFat)}g"
                        carbsTextView.text = "Carbs: ${decimalFormat.format(totalCarbohydrates)}g"
                    } else {
                        // nie ma posiłków w dokumencie, nie wyświetlam nic
                        mealListAdapter.setMeals(emptyList())

                        caloriesTextView.text = R.string.defaultSumCal.toString()
                        fatTextView.text = R.string.defaultSumFat.toString()
                        carbsTextView.text = R.string.defaultSumCarbs.toString()
                    }

                    targetTextView.text = "Calories goal: $target kcal"
                } else {
                    // nie ma dokumentu, nie wyświetlam nic
                    mealListAdapter.setMeals(emptyList())

                    caloriesTextView.text = R.string.defaultSumCal.toString()
                    fatTextView.text = R.string.defaultSumFat.toString()
                    carbsTextView.text = R.string.defaultSumCarbs.toString()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching meals", exception)
                Toast.makeText(requireContext(), "Error fetching meals.", Toast.LENGTH_LONG)
                mealListAdapter.setMeals(emptyList())

                caloriesTextView.text = R.string.defaultSumCal.toString()
                fatTextView.text = R.string.defaultSumFat.toString()
                carbsTextView.text = R.string.defaultSumCarbs.toString()
            }
    }
}