package model

enum class EquipmentStatus(
    val title: String
) {
    IN_STOCK("На складе"),
    IN_GROUP("В группе"),
    WRITTEN_OFF("Списано")
}