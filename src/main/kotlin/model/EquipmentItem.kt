package model

data class EquipmentItem(
    val inventoryId: String,
    val typeName: String,
    val serialNumber: String?,
    val quantity: Int,
    val status: EquipmentStatus,
    val currentGroup: String?,
    val currentLocation: String?,
    val completenessStatus: CompletenessStatus,
    val missingParts: String? = null
)