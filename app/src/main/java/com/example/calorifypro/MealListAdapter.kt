package com.example.calorifypro

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MealListAdapter(private val showButton: Boolean) : RecyclerView.Adapter<MealListAdapter.ViewHolder>() {

    interface MealItemClickListener {
        fun onMealItemClick(name: String, calories: String, fat: String, carbohydrates: String)
    }

    private var meals: List<Map<String, Any>> = emptyList()
    private var mealItemClickListener: MealItemClickListener? = null

    fun setMeals(meals: List<Map<String, Any>>) {
        this.meals = meals
        notifyDataSetChanged()
    }

    fun getMealName(position: Int): String {
        return meals.getOrNull(position)?.get("name") as? String ?: ""
    }

    fun getMealCalories(position: Int): String {
        return meals.getOrNull(position)?.get("calories").toString()
    }

    fun getMealFat(position: Int): String {
        return meals.getOrNull(position)?.get("fat").toString()
    }

    fun getMealCarbohydrates(position: Int): String {
        return meals.getOrNull(position)?.get("carbohydrates").toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.meal_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal)

        if (showButton) {
            holder.itemView.findViewById<ImageButton>(R.id.buttonDelete).visibility = View.VISIBLE
            holder.itemView.findViewById<ImageButton>(R.id.buttonDelete).setOnClickListener {
                mealItemClickListener?.onMealItemClick(
                    getMealName(position),
                    getMealCalories(position),
                    getMealFat(position),
                    getMealCarbohydrates(position)
                )
            }
        } else {
            holder.itemView.findViewById<ImageButton>(R.id.buttonDelete).visibility = View.GONE
        }
    }

    fun setMealItemClickListener(listener: MealItemClickListener) {
        mealItemClickListener = listener
    }


    override fun getItemCount(): Int {
        return meals.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textName)
        private val textCalories: TextView = itemView.findViewById(R.id.textCalories)
        private val textFat: TextView = itemView.findViewById(R.id.textFat)
        private val textCarbohydrates: TextView = itemView.findViewById(R.id.textCarbohydrates)

        init {
            // Obsluga przycisku usuwania
            itemView.findViewById<ImageButton>(R.id.buttonDelete).setOnClickListener {
                val meal = meals[adapterPosition]
                val mealName = meal["name"].toString()
                val mealCalories = meal["calories"]?.toString() ?: ""
                val mealFat = meal["fat"]?.toString() ?: ""
                val mealCarbohydrates = meal["carbohydrates"]?.toString() ?: ""

                mealItemClickListener?.onMealItemClick(mealName, mealCalories, mealFat, mealCarbohydrates)
            }
        }

        private fun formatNumber(value: String): String {
            return when {
                value.contains(".") -> {
                    val formattedValue = String.format("%.2f", value.toDouble())
                    if (formattedValue.endsWith(".00")) {
                        formattedValue.substring(0, formattedValue.length - 3)
                    } else if (formattedValue.endsWith("0")) {
                        formattedValue.substring(0, formattedValue.length - 1)
                    } else {
                        formattedValue
                    }
                }
                else -> value
            }
        }

        fun bind(meal: Map<String, Any>) {
            textName.text = "${meal["name"]}"
            textCalories.text = "Calories: ${formatNumber(meal["calories"]?.toString() ?: "")}kcal"
            textFat.text = "Fat: ${formatNumber(meal["fat"]?.toString() ?: "")}g"
            textCarbohydrates.text = "Carbohydrates: ${formatNumber(meal["carbohydrates"]?.toString() ?: "")}g"
        }

    }
}

