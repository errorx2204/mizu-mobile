package com.rushov.mizu.presentation.utils

object CategoriesHelper {
    val incomeCategories = mapOf(
        "Salary" to listOf("Monthly Salary", "Bonus", "Overtime Pay", "Commission"),
        "Freelance" to listOf("Client Project", "Consulting", "Design Work", "Development"),
        "Investment" to listOf("Stock Dividend", "Interest", "Mutual Fund", "Crypto Profit"),
        "Gifts" to listOf("Birthday Gift", "Festival Gift", "Wedding Gift", "Cash Gift"),
        "Selling" to listOf("Old Phone", "Furniture", "Clothes", "Electronics"),
        "Other Income" to listOf("Refund", "Cashback", "Reward", "Other"),
    )
    
    val expenseCategories = mapOf(
        "Food" to listOf("Groceries", "Restaurant", "Street Food", "Coffee", "Fancy Dinner", "Lunch", "Breakfast"),
        "Housing" to listOf("Rent", "Maintenance", "Property Tax", "Furniture", "Home Decor"),
        "Transport" to listOf("Petrol", "Cab", "Bus", "Train", "Flight", "Auto", "Bike Service"),
        "Shopping" to listOf("Clothes", "Shoes", "Electronics", "Amazon", "Flipkart", "Accessories"),
        "Entertainment" to listOf("Netflix", "Movie", "Concert", "Weekend Trip", "Gaming", "Subscription"),
        "Health" to listOf("Gym", "Doctor", "Medicine", "Health Insurance", "Dental", "Eye Checkup"),
        "Utilities" to listOf("Electricity", "Water", "Mobile Recharge", "WiFi", "Gas", "DTH"),
        "Education" to listOf("Course Fee", "Books", "Online Course", "Exam Fee", "Workshop"),
        "Personal" to listOf("Haircut", "Salon", "Skincare", "Gifts", "Donation"),
        "Other Expense" to listOf("Miscellaneous", "Penalty", "Fine", "Other"),
    )
    
    fun getCategories(type: String): Map<String, List<String>> {
        return if (type == "income") incomeCategories else expenseCategories
    }
}
