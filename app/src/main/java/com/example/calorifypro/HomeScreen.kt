package com.example.calorifypro

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class HomeScreen : Fragment(), MealListAdapter.MealItemClickListener {

   private lateinit var dateTextView : TextView
   private lateinit var recyclerViewFood : RecyclerView
   private lateinit var addButton : Button
   private lateinit var targetTextView: TextView
   private lateinit var targetEditText: EditText
   private lateinit var targetEditButton: ImageButton
   private lateinit var targetCancelEditButton : ImageButton
   private lateinit var caloriesTextView: TextView
   private lateinit var fatTextView: TextView
   private lateinit var carbsTextView: TextView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentDate : String
    private lateinit var mealListAdapter: MealListAdapter
    private lateinit var email : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        email = sharedPreferencesManager.userEmail!!

        firestore = FirebaseFirestore.getInstance()

        dateTextView = view.findViewById(R.id.dateTextView)
        recyclerViewFood = view.findViewById(R.id.recyclerViewFood)
        addButton = view.findViewById(R.id.buttonAddMeal)
        targetTextView = view.findViewById(R.id.targetTextView)
        targetEditText = view.findViewById(R.id.targetEditText)
        targetEditButton = view.findViewById(R.id.targetEditButton)
        targetCancelEditButton = view.findViewById(R.id.targetCancelEditButton)
        caloriesTextView = view.findViewById(R.id.caloriesTextView)
        fatTextView = view.findViewById(R.id.fatTextView)
        carbsTextView = view.findViewById(R.id.carbsTextView)

        val formatterText = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        currentDate = LocalDateTime.now().format(formatterText)
        dateTextView.text = currentDate


        // POBIERANKO POSILKOW Z BAZY
        mealListAdapter = MealListAdapter(true)

        recyclerViewFood.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewFood.adapter = mealListAdapter


        // KASUJEMY ITEM JAK JEST WCISNIETY PRZYCISK, BENC
        mealListAdapter.setMealItemClickListener(this)


        // PRZYCISK EDYCJI TARGETU
            targetEditButton.setOnClickListener {
                // ukryj TextView, pokaz EditText
                targetTextView.visibility = View.INVISIBLE
                targetEditText.visibility = View.VISIBLE

                //ukryj przycisk, pokaz x
                targetEditButton.visibility = View.GONE
                targetCancelEditButton.visibility = View.VISIBLE


                // Przenieś focus na EditText
                targetEditText.requestFocus()

                // Otwórz klawiaturę
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(targetEditText, InputMethodManager.SHOW_IMPLICIT)
            }

        // CANCEL EDYCJI
        targetCancelEditButton.setOnClickListener {
            // pokaz TextView, ukryj EditText
            targetTextView.visibility = View.VISIBLE
            targetEditText.visibility = View.GONE
            targetEditText.text.clear()

            //pokaz przycisk, ukryj x
            targetEditButton.visibility = View.VISIBLE
            targetCancelEditButton.visibility = View.GONE

            view.clearFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(targetEditText.windowToken, 0)
        }

        //EDYCJA TARGETU
        targetEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {

                if (targetEditText.text.isEmpty() || targetEditText.text.length > 5 || targetEditText.text.toString().toInt() == 0) {
                    Toast.makeText(requireContext(),"Goal value must be a reasonable number",Toast.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }

                updateTargetInFirestore(targetEditText.text.toString().toLong())
                targetTextView.text = "Calories goal: " + targetEditText.text.toString().toInt() + " kcal"
                getMealsFromFirestore()

                targetEditText.text.clear()

                // pokaz TextView, ukryj EditText
                targetTextView.visibility = View.VISIBLE
                targetEditText.visibility = View.GONE

                //pokaz przycisk, ukryj x
                targetEditButton.visibility = View.VISIBLE
                targetCancelEditButton.visibility = View.GONE

                view.clearFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(targetEditText.windowToken, 0)

                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }


        // EKRAN DODAWANIA
        addButton.setOnClickListener(){
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out
            )
            fragmentTransaction.replace(R.id.fragment_container_app, AddMealScreen())
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        getMealsFromFirestore()

        return view
    }

    private fun updateTargetInFirestore(newTarget: Long) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val documentName = dateFormat.format(Calendar.getInstance().time)

        val documentReference = firestore.collection("users")
            .document(email)
            .collection("days")
            .document(documentName)

        documentReference.update("target", newTarget)
            .addOnSuccessListener {
                Log.d("Firestore", "Target updated successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error updating target", exception)
            }
    }


    private fun getMealsFromFirestore() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val documentName = dateFormat.format(Calendar.getInstance().time)

        firestore.collection("users")
            .document(email)
            .collection("days")
            .document(documentName)
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

                        caloriesTextView.text = getString(R.string.defaultSumCal)
                        fatTextView.text = getString(R.string.defaultSumFat)
                        carbsTextView.text = getString(R.string.defaultSumCarbs)
                    }

                    targetTextView.text = "Calories goal: $target kcal"

                    if (target.toString().isNullOrEmpty())
                    {
                        targetTextView.text = "Calories goal: kcal"
                        caloriesTextView.text = getString(R.string.defaultSumCal)
                        fatTextView.text = getString(R.string.defaultSumFat)
                        carbsTextView.text = getString(R.string.defaultSumCarbs)
                    }

                } else {
                    // nie ma dokumentu, nie wyświetlam nic
                    mealListAdapter.setMeals(emptyList())

                    caloriesTextView.text = getString(R.string.defaultSumCal)
                    fatTextView.text = getString(R.string.defaultSumFat)
                    carbsTextView.text = getString(R.string.defaultSumCarbs)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching meals", exception)
                Toast.makeText(requireContext(), "Error fetching meals.", Toast.LENGTH_LONG)
                mealListAdapter.setMeals(emptyList())

                caloriesTextView.text = getString(R.string.defaultSumCal)
                fatTextView.text = getString(R.string.defaultSumFat)
                carbsTextView.text = getString(R.string.defaultSumCarbs)
            }
    }


    //KASUJEMY GOŚCIA
    override fun onMealItemClick(name: String, calories: String, fat: String, carbohydrates: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val documentName = dateFormat.format(Calendar.getInstance().time)

        val mealToDelete = mapOf(
            "name" to name,
            "calories" to calories.toDouble(),
            "fat" to fat.toDouble(),
            "carbohydrates" to carbohydrates.toDouble()
        )

        firestore.collection("users")
            .document(email)
            .collection("days")
            .document(documentName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val meals = document.data?.get("meals") as? MutableList<Map<String, Any>>

                    meals?.let {
                        val index = it.indexOf(mealToDelete)

                        if (index != -1) {
                            it.removeAt(index)

                            firestore.collection("users")
                                .document(email)
                                .collection("days")
                                .document(documentName)
                                .update("meals", it)
                                .addOnFailureListener { exception ->
                                    Toast.makeText(requireContext(),"Error deleting meal.",Toast.LENGTH_SHORT).show()
                                }
                            getMealsFromFirestore()
                        }
                    }
                }
            }
    }
}