package model

// MovementType = Тип движения
enum class MovementType(
    val title: String
) {
    RECEIPT("Поступление"),
    TRANSFER("Передача"),
    RETURN("Возврат"),
    WRITE_OFF("Списание")
}