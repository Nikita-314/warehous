package model

data class Movement(
    val inventoryId: String, // Какое оборудование перемещали
    val date: String,
    val documentNumber: String,
    val fromGroup: String, // Откуда передали
    val toGroup: String, // Куда передали
    val location: String?, // Объект эксплуатации
    val transferredBy: String, // Кто передал
    val acceptedBy: String // Кто принял
)