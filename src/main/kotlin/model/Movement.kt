package model

data class Movement(
    val movementType: MovementType,
    val inventoryId: String,
    val typeName: String,
    val serialNumber: String?,
    val quantity: Int,
    val date: String,
    val documentNumber: String,
    val source: String,
    val destination: String,
    val location: String?,
    val transferredBy: String,
    val acceptedBy: String,
    val completenessStatus: CompletenessStatus,
    val missingParts: String?
)