package com.example.calorifypro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddMealScreen : Fragment() {

    private lateinit var toolbar: Toolbar
    private lateinit var editTextName: EditText
    private lateinit var editTextCalories: EditText
    private lateinit var editTextFat: EditText
    private lateinit var editTextCarbohydrates: EditText
    private lateinit var textViewError: TextView
    private lateinit var buttonAddMeal: Button
    private lateinit var editTextQuantity : EditText

    private lateinit var firestore: FirebaseFirestore
    lateinit var email : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_meal, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        editTextName = view.findViewById(R.id.editTextName)
        editTextCalories = view.findViewById(R.id.editTextCalories)
        editTextFat = view.findViewById(R.id.editTextFat)
        editTextCarbohydrates = view.findViewById(R.id.editTextCarbohydrates)
        textViewError = view.findViewById(R.id.textViewError)
        buttonAddMeal = view.findViewById(R.id.buttonAddMeal)
        editTextQuantity = view.findViewById(R.id.editTextQuantity)

        firestore = FirebaseFirestore.getInstance()

        //filtrujemy inputy
        val mealInputFilter = MealInputFilter()
        editTextQuantity.filters = arrayOf(mealInputFilter)
        editTextCalories.filters = arrayOf(mealInputFilter)
        editTextFat.filters = arrayOf(mealInputFilter)
        editTextCarbohydrates.filters = arrayOf(mealInputFilter)


        //pobranie email z pamieci
        val sharedPreferencesManager = SharedPreferencesManager(requireContext())
        email = sharedPreferencesManager.userEmail!!

        // back button na pasku gornym
        toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        buttonAddMeal.setOnClickListener {
            if (editTextCalories.text.isEmpty() || editTextCarbohydrates.text.isEmpty() || editTextFat.text.isEmpty() || editTextName.text.isEmpty() || editTextQuantity.text.isEmpty()) {
                textViewError.text = "All fields must be filled."
                return@setOnClickListener
            }

            addMeal()
        }

        return view
    }

    private fun addMeal() {
        val quantity = editTextQuantity.text.toString().toDouble()/100
        val name = editTextName.text.toString()
        val calories = editTextCalories.text.toString().toDouble()*quantity
        val fat = editTextFat.text.toString().toDouble()*quantity
        val carbohydrates = editTextCarbohydrates.text.toString().toDouble()*quantity

        val mealData = mapOf(
            "name" to name,
            "calories" to calories,
            "fat" to fat,
            "carbohydrates" to carbohydrates
        )

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val documentName = dateFormat.format(Calendar.getInstance().time)

        firestore.collection("users")
            .document(email!!)
            .collection("days")
            .document(documentName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val meals = document.data?.get("meals") as? List<Map<String, Any>>

                    meals?.let {
                        val mutableMeals = it.toMutableList()
                        mutableMeals.add(mealData)

                        // Aktualizacja dokumentu w bazie danych
                        firestore.collection("users")
                            .document(email)
                            .collection("days")
                            .document(documentName)
                            .update("meals", mutableMeals)
                            .addOnSuccessListener {
                                // Pomyślnie dodano posiłek
                                Toast.makeText(requireContext(), "Meal added successfully.", Toast.LENGTH_SHORT).show()
                                requireActivity().onBackPressed()
                            }
                            .addOnFailureListener { exception ->
                                // Błąd podczas aktualizacji dokumentu
                                textViewError.text = "Error adding meal: ${exception.message}"
                            }
                    }
                } else {
                    // Dokument nie istnieje
                    textViewError.text = "Error adding meal: Document does not exist."
                }
            }
            .addOnFailureListener { exception ->
                // Błąd podczas pobierania dokumentu
                textViewError.text = "Error adding meal: ${exception.message}"
            }
    } // addMeal


}
