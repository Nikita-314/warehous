package service

import model.CompletenessStatus
import model.EquipmentItem
import model.EquipmentStatus

// EquipmentService = сервис работы с оборудованием
class EquipmentService {
    private var nextInventoryNumber = 1
    private fun generateInventoryId(): String {
        val id = "INV-" + nextInventoryNumber.toString().padStart(6, '0')
        nextInventoryNumber++
        return id
    }

    private val items = mutableListOf<EquipmentItem>()

    fun addEquipment(
        typeName: String,
        serialNumber: String?,
        quantity: Int
    ) {
        if (quantity <= 0) {
            println("Количество должно быть больше 0")
            return
        }

        if (serialNumber != null && quantity != 1) {
            println("Если есть серийный номер, количество должно быть 1")
            return
        }
        val inventoryId = generateInventoryId()

        val item = EquipmentItem(
            inventoryId = inventoryId,
            typeName = typeName,
            serialNumber = serialNumber,
            quantity = quantity,
            status = EquipmentStatus.IN_STOCK,
            currentGroup = null,
            currentLocation = null,
            completenessStatus = CompletenessStatus.COMPLETE
        )

        items.add(item)

        println("Оборудование добавлено: $typeName")
    }

    fun getAllItems(): List<EquipmentItem> {
        return items
    }
}