package com.example.uniqo

enum class LookingForGroup(val label: String) {
    HOUSING("Housing"),
    MARKETPLACE("Marketplace")
}

enum class FieldType { TEXT, CHIPS_SINGLE, CHIPS_MULTI, BUDGET_RANGE, AREAS_MULTI, LOCATION }

data class FieldSpec(
    val key: String,
    val label: String,
    val type: FieldType,
    val options: List<String> = emptyList()
)

enum class LookingForCategory(
    val label: String,
    val group: LookingForGroup,
    val icon: String,
    val description: String
) {
    ROOMMATE("Roommate", LookingForGroup.HOUSING, "👥", "Find someone to share a place with"),
    ROOM("Room", LookingForGroup.HOUSING, "🛏️", "Find a room to move into"),
    FLAT("Flat", LookingForGroup.HOUSING, "🏠", "Find a full flat or apartment"),
    FIND_TOGETHER("Find Together", LookingForGroup.HOUSING, "🤝", "Team up with others for a new place"),
    REPLACEMENT("Replacement", LookingForGroup.HOUSING, "🔄", "Find or become a replacement tenant"),
    TENANTS("Tenants", LookingForGroup.HOUSING, "🏢", "List a vacancy for tenants"),

    FURNITURE("Furniture", LookingForGroup.MARKETPLACE, "🪑", "Chairs, tables, beds, and more"),
    ELECTRONICS("Electronics", LookingForGroup.MARKETPLACE, "💻", "Laptops, phones, and gadgets"),
    BOOKS("Books", LookingForGroup.MARKETPLACE, "📚", "Academic and other books"),
    APPLIANCES("Appliances", LookingForGroup.MARKETPLACE, "🧺", "Washing machines, fridges, and more"),
    CYCLES("Cycles", LookingForGroup.MARKETPLACE, "🚲", "Bicycles of any type"),
    BIKES("Bikes", LookingForGroup.MARKETPLACE, "🏍️", "Motorcycles and scooters"),
    HOME_DECOR("Home & Decor", LookingForGroup.MARKETPLACE, "🪴", "Plants, lamps, and decor items"),
    ACCESSORIES("Accessories", LookingForGroup.MARKETPLACE, "🎒", "Bags, watches, and accessories"),
    OTHER("Other", LookingForGroup.MARKETPLACE, "📦", "Anything else")
}

data class LookingForPreference(
    val id: String,
    val category: LookingForCategory,
    val isActive: Boolean = true,
    val fields: Map<String, String> = emptyMap(),
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun summaryLine(): String {
        val budgetMin = fields["budgetMin"]
        val budgetMax = fields["budgetMax"]
        val budget = if (!budgetMin.isNullOrBlank() && !budgetMax.isNullOrBlank()) "₹${budgetMin}–₹$budgetMax" else null
        val typeField = fields["roomType"] ?: fields["itemType"] ?: fields["flatType"]
        return listOfNotNull(typeField, budget).joinToString(" • ").ifBlank { category.description }
    }

    fun locationSummary(): String {
        val exact = fields["exactLocation"]?.substringAfter("|", missingDelimiterValue = "")
        if (!exact.isNullOrBlank()) return exact

        val areas = fields["areas"]?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        return when {
            areas.isEmpty() -> "Any location"
            areas.size == 1 -> areas.first()
            else -> "${areas.first()} + ${areas.size - 1} areas"
        }
    }
}

/** Field definitions per category — this is what drives the generic form screen. */
object LookingForFieldSpecs {
    private val budget = FieldSpec("budget", "Budget", FieldType.BUDGET_RANGE)
    private val areas = FieldSpec("areas", "Preferred Areas", FieldType.AREAS_MULTI)
    private val exactLocation = FieldSpec("exactLocation", "Exact Location", FieldType.LOCATION)
    private val furnishing = FieldSpec("furnishing", "Furnishing", FieldType.CHIPS_SINGLE, listOf("Furnished", "Semi Furnished", "Unfurnished", "Any"))
    private val moveIn = FieldSpec("moveInTimeline", "Move-in Timeline", FieldType.CHIPS_SINGLE, listOf("Immediate", "Within 2 weeks", "Within a month", "Flexible"))
    private val stayDuration = FieldSpec("stayDuration", "Stay Duration", FieldType.CHIPS_SINGLE, listOf("Short term", "6 months", "1 year", "Long term"))
    private val condition = FieldSpec("condition", "Condition", FieldType.CHIPS_SINGLE, listOf("New", "Like New", "Good", "Used", "Any"))

    fun forCategory(category: LookingForCategory): List<FieldSpec> = when (category) {
        LookingForCategory.ROOMMATE -> listOf(
            FieldSpec("numberOfRoommates", "Number of Roommates", FieldType.CHIPS_SINGLE, listOf("1", "2", "3+", "Any")),
            FieldSpec("genderPref", "Gender", FieldType.CHIPS_SINGLE, listOf("Male", "Female", "Any", "No Preference")),
            FieldSpec("ageRange", "Age", FieldType.CHIPS_SINGLE, listOf("18–22", "22–26", "26–30", "30+", "No Preference")),
            FieldSpec("occupation", "Occupation", FieldType.CHIPS_SINGLE, listOf("Student", "Professional", "Freelancer", "Entrepreneur", "Any")),
            FieldSpec("lifestyle", "Lifestyle", FieldType.CHIPS_MULTI, listOf("Clean", "Chill", "Quiet", "Social", "Party", "WFH")),
            FieldSpec("sleepSchedule", "Sleep Schedule", FieldType.CHIPS_SINGLE, listOf("Early Bird", "Night Owl", "Flexible")),
            FieldSpec("smoking", "Smoking", FieldType.CHIPS_SINGLE, listOf("Yes", "No", "Doesn't Matter")),
            FieldSpec("drinking", "Drinking", FieldType.CHIPS_SINGLE, listOf("Yes", "No", "Doesn't Matter")),
            FieldSpec("pets", "Pets", FieldType.CHIPS_SINGLE, listOf("Yes", "No", "Doesn't Matter")),
            FieldSpec("cleanliness", "Cleanliness", FieldType.CHIPS_SINGLE, listOf("Low", "Medium", "High")),
            FieldSpec("guests", "Guests", FieldType.CHIPS_SINGLE, listOf("Frequently", "Occasionally", "Rarely")),
            budget, areas, exactLocation
        )
        LookingForCategory.ROOM -> listOf(
            budget,
            FieldSpec("utilitiesIncluded", "Utilities", FieldType.CHIPS_SINGLE, listOf("Included", "Extra", "Doesn't Matter")),
            stayDuration, moveIn, areas, exactLocation,
            FieldSpec("roomType", "Room Type", FieldType.CHIPS_SINGLE, listOf("Private", "Shared", "Any")),
            FieldSpec("bathroom", "Bathroom", FieldType.CHIPS_SINGLE, listOf("Attached", "Shared", "Any")),
            furnishing,
            FieldSpec("amenities", "Amenities", FieldType.CHIPS_MULTI, listOf("AC", "Wi-Fi", "Washing Machine", "Kitchen", "Parking", "Balcony", "Lift", "Geyser")),
            FieldSpec("flatmates", "Flatmates", FieldType.CHIPS_SINGLE, listOf("Male", "Female", "Mixed", "Students", "Professionals")),
            FieldSpec("lifestyle", "Lifestyle", FieldType.CHIPS_MULTI, listOf("Clean", "Quiet", "Social", "Work Friendly")),
            FieldSpec("otherPrefs", "Other Preferences", FieldType.CHIPS_MULTI, listOf("Non-Smoking", "Vegetarian", "Pets OK", "Guests OK"))
        )
        LookingForCategory.FLAT -> listOf(
            budget,
            FieldSpec("flatType", "Flat Type", FieldType.CHIPS_SINGLE, listOf("1RK", "1BHK", "2BHK", "3BHK", "4BHK+")),
            areas, exactLocation, furnishing, moveIn, stayDuration,
            FieldSpec("propertyType", "Property Type", FieldType.CHIPS_SINGLE, listOf("Apartment", "House", "Studio", "Gated Society")),
            FieldSpec("amenities", "Amenities", FieldType.CHIPS_MULTI, listOf("Parking", "Lift", "Security", "Power Backup", "Gym", "Pool", "Balcony")),
            FieldSpec("occupants", "Occupants", FieldType.CHIPS_SINGLE, listOf("1", "2", "3", "4+"))
        )
        LookingForCategory.FIND_TOGETHER -> listOf(
            FieldSpec("peopleNeeded", "People Needed", FieldType.CHIPS_SINGLE, listOf("1", "2", "3+")),
            areas, exactLocation,
            FieldSpec("budgetPerPerson", "Budget per Person", FieldType.BUDGET_RANGE),
            FieldSpec("flatType", "Flat Type", FieldType.CHIPS_SINGLE, listOf("1RK", "1BHK", "2BHK", "3BHK", "4BHK+")),
            FieldSpec("roomType", "Room Type", FieldType.CHIPS_SINGLE, listOf("Private", "Shared")),
            furnishing, moveIn, stayDuration,
            FieldSpec("preferredGender", "Preferred Gender", FieldType.CHIPS_SINGLE, listOf("Male", "Female", "Any")),
            FieldSpec("preferredType", "Preferred Type", FieldType.CHIPS_SINGLE, listOf("Student", "Professional", "Any")),
            FieldSpec("lifestyle", "Lifestyle", FieldType.CHIPS_MULTI, listOf("Clean", "Quiet Sleep", "Non-Smoking", "No Drinking", "Pets OK", "Guests OK"))
        )
        LookingForCategory.REPLACEMENT -> listOf(
            FieldSpec("replacementType", "Type", FieldType.CHIPS_SINGLE, listOf("Replacing Myself", "Looking for Replacement Room")),
            FieldSpec("flatType", "Flat Type", FieldType.CHIPS_SINGLE, listOf("1RK", "1BHK", "2BHK", "3BHK", "4BHK+")),
            FieldSpec("roomType", "Room Type", FieldType.CHIPS_SINGLE, listOf("Private", "Shared")),
            FieldSpec("monthlyRent", "Monthly Rent", FieldType.BUDGET_RANGE),
            FieldSpec("securityDeposit", "Security Deposit", FieldType.TEXT),
            FieldSpec("moveInDate", "Move-in Date", FieldType.TEXT),
            stayDuration, areas, exactLocation, furnishing,
            FieldSpec("replacementTenant", "Replacement Tenant", FieldType.CHIPS_SINGLE, listOf("Male", "Female", "Any", "Student", "Professional")),
            FieldSpec("existingFlatmateDetails", "Existing Flatmate Details", FieldType.TEXT),
            FieldSpec("amenities", "Amenities", FieldType.CHIPS_MULTI, listOf("AC", "Wi-Fi", "Parking", "Lift")),
            FieldSpec("additionalInfo", "Additional Information", FieldType.TEXT)
        )
        LookingForCategory.TENANTS -> listOf(
            FieldSpec("propertyType", "Property Type", FieldType.CHIPS_SINGLE, listOf("Apartment", "House", "Studio", "PG")),
            FieldSpec("unitType", "Unit Type", FieldType.CHIPS_SINGLE, listOf("Private", "Shared", "Entire Flat")),
            FieldSpec("rentRange", "Rent Range", FieldType.BUDGET_RANGE),
            FieldSpec("securityDeposit", "Security Deposit", FieldType.TEXT),
            FieldSpec("vacancies", "Vacancies", FieldType.CHIPS_SINGLE, listOf("1", "2", "3+")),
            FieldSpec("tenantType", "Tenant Type", FieldType.CHIPS_MULTI, listOf("Student", "Professional", "Family", "Male", "Female", "Any")),
            areas, exactLocation, furnishing,
            FieldSpec("amenities", "Amenities", FieldType.CHIPS_MULTI, listOf("Wi-Fi", "AC", "Parking", "Lift", "Security", "Kitchen", "Washing Machine")),
            FieldSpec("availableFrom", "Available From", FieldType.TEXT),
            FieldSpec("minimumStay", "Minimum Stay", FieldType.TEXT)
        )
        LookingForCategory.FURNITURE -> listOf(
            FieldSpec("itemType", "Type", FieldType.CHIPS_SINGLE, listOf("Chair", "Table", "Sofa", "Bed", "Mattress", "Desk", "Wardrobe", "Study Table", "Other")),
            budget, condition,
            FieldSpec("material", "Material", FieldType.TEXT),
            FieldSpec("assembly", "Assembly Required", FieldType.CHIPS_SINGLE, listOf("Yes", "No", "Doesn't Matter")),
            areas, exactLocation,
            FieldSpec("delivery", "Delivery Needed", FieldType.CHIPS_SINGLE, listOf("Yes", "No"))
        )
        LookingForCategory.ELECTRONICS -> listOf(
            FieldSpec("itemType", "Category", FieldType.CHIPS_SINGLE, listOf("Laptop", "Phone", "Tablet", "Monitor", "TV", "Keyboard", "Mouse", "Headphones", "Camera", "Other")),
            budget, condition,
            FieldSpec("brand", "Brand", FieldType.TEXT),
            FieldSpec("age", "Age", FieldType.TEXT),
            FieldSpec("warranty", "Warranty", FieldType.CHIPS_SINGLE, listOf("Yes", "No", "Doesn't Matter")),
            areas, exactLocation
        )
        LookingForCategory.BOOKS -> listOf(
            FieldSpec("itemType", "Type", FieldType.CHIPS_SINGLE, listOf("Academic", "Competitive", "Fiction", "Non-fiction", "Self Help", "Engineering", "Medical", "Other")),
            FieldSpec("subjectGenre", "Subject / Genre", FieldType.TEXT),
            condition, budget,
            FieldSpec("bundleType", "Bundle", FieldType.CHIPS_SINGLE, listOf("Single", "Bundle")),
            areas, exactLocation
        )
        LookingForCategory.APPLIANCES -> listOf(
            FieldSpec("itemType", "Type", FieldType.CHIPS_SINGLE, listOf("Washing Machine", "Refrigerator", "Microwave", "AC", "Cooler", "Fan", "Geyser", "Induction", "Other")),
            budget, condition,
            FieldSpec("brand", "Brand", FieldType.TEXT),
            FieldSpec("capacity", "Capacity", FieldType.TEXT),
            FieldSpec("energyRating", "Energy Rating", FieldType.TEXT),
            areas, exactLocation
        )
        LookingForCategory.CYCLES -> listOf(
            FieldSpec("itemType", "Type", FieldType.CHIPS_SINGLE, listOf("Mountain", "Road", "Hybrid", "City", "Folding", "BMX", "Any")),
            budget, condition,
            FieldSpec("frameSize", "Frame Size", FieldType.TEXT),
            FieldSpec("brand", "Brand", FieldType.TEXT),
            FieldSpec("gearType", "Gear Type", FieldType.TEXT),
            FieldSpec("usage", "Usage", FieldType.TEXT),
            areas, exactLocation,
            FieldSpec("radiusKm", "Search Radius", FieldType.TEXT)
        )
        LookingForCategory.BIKES -> listOf(
            FieldSpec("itemType", "Type", FieldType.CHIPS_SINGLE, listOf("Motorcycle", "Scooter", "Any")),
            budget, condition,
            FieldSpec("brand", "Brand", FieldType.TEXT),
            FieldSpec("model", "Model", FieldType.TEXT),
            FieldSpec("year", "Year", FieldType.TEXT),
            FieldSpec("fuelType", "Fuel", FieldType.CHIPS_SINGLE, listOf("Petrol", "Electric", "Other")),
            FieldSpec("transmission", "Transmission", FieldType.TEXT),
            areas, exactLocation
        )
        LookingForCategory.HOME_DECOR -> listOf(
            FieldSpec("itemType", "Category", FieldType.CHIPS_SINGLE, listOf("Plants", "Lamps", "Curtains", "Wall Decor", "Rugs", "Mirrors", "Storage", "Kitchen Decor", "Other")),
            budget, condition, areas, exactLocation
        )
        LookingForCategory.ACCESSORIES -> listOf(
            FieldSpec("itemType", "Category", FieldType.CHIPS_SINGLE, listOf("Bags", "Watches", "Headphones", "Backpacks", "College", "Sports", "Other")),
            budget, condition,
            FieldSpec("brand", "Brand", FieldType.TEXT),
            areas, exactLocation
        )
        LookingForCategory.OTHER -> listOf(
            FieldSpec("itemName", "Item Name", FieldType.TEXT),
            FieldSpec("description", "Description", FieldType.TEXT),
            budget, condition, areas, exactLocation,
            FieldSpec("additionalInfo", "Additional Information", FieldType.TEXT)
        )
    }
}