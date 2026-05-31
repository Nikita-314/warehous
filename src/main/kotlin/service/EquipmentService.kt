package service
import model.Movement
import model.MovementType
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
    private val movements = mutableListOf<Movement>()

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
            completenessStatus = CompletenessStatus.COMPLETE,
            missingParts = null
        )

        items.add(item)

        println("Оборудование добавлено: $typeName")
    }

    fun getAllItems(): List<EquipmentItem> {
        return items
    }

    fun transferEquipment(
        inventoryId: String,
        newGroup: String,
        newLocation: String?,
        date: String,
        documentNumber: String,
        transferredBy: String,
        acceptedBy: String,
        movementType: MovementType = MovementType.TRANSFER
    ) {
        for (index in items.indices) {
            val item = items[index]

            if (item.inventoryId == inventoryId) {

                val oldPlace = item.currentGroup ?: "Склад"
                if (oldPlace == newGroup) {
                    println("Оборудование $inventoryId уже находится в группе $newGroup")
                    return
                }

                val movement = Movement(
                    movementType = movementType,
                    inventoryId = item.inventoryId,
                    typeName = item.typeName,
                    serialNumber = item.serialNumber,
                    quantity = item.quantity,
                    date = date,
                    documentNumber = documentNumber,
                    source = oldPlace,
                    destination = newGroup,
                    location = newLocation,
                    transferredBy = transferredBy,
                    acceptedBy = acceptedBy,
                    completenessStatus = item.completenessStatus,
                    missingParts = item.missingParts
                )

                movements.add(movement)

                val updatedItem = item.copy(
                    status = EquipmentStatus.IN_GROUP,
                    currentGroup = newGroup,
                    currentLocation = newLocation
                )

                items[index] = updatedItem

                if (movementType == MovementType.RETURN) {
                    println("Оборудование $inventoryId возвращено на склад")
                } else {
                    println("Оборудование $inventoryId передано в группу $newGroup")
                }
                return
            }
        }

        println("Оборудование с номером $inventoryId не найдено")
    }

    fun getAllMovements(): List<Movement> {
        return movements
    }

    fun loadInitialData(
        loadedItems: List<EquipmentItem>,
        loadedMovements: List<Movement>
    ) {
        items.clear()
        items.addAll(loadedItems)
        movements.clear()
        movements.addAll(loadedMovements)
        nextInventoryNumber = findNextInventoryNumber()
    }
    private fun findNextInventoryNumber(): Int {
        var maxNumber = 0

        for (item in items) {
            val numberText = item.inventoryId.removePrefix("INV-")
            val number = numberText.toInt()

            if (number > maxNumber) {
                maxNumber = number
            }
        }

        return maxNumber + 1
    }

    fun findEquipmentById(inventoryId: String): EquipmentItem? {
        for (item in items) {
            if (item.inventoryId == inventoryId) {
                return item
            }
        }

        return null
    }

    fun getMovementCount(inventoryId: String): Int {
        var count = 0

        for (movement in movements) {
            if (movement.inventoryId == inventoryId) {
                count++
            }
        }

        return count
    }

    fun getLastMovement(inventoryId: String): Movement? {
        var lastMovement: Movement? = null

        for (movement in movements) {
            if (movement.inventoryId == inventoryId) {
                lastMovement = movement
            }
        }

        return lastMovement
    }

    fun returnEquipment(
        inventoryId: String,
        date: String,
        documentNumber: String,
        transferredBy: String,
        acceptedBy: String
    ) {
        transferEquipment(
            inventoryId = inventoryId,
            newGroup = "Склад",
            newLocation = null,
            date = date,
            documentNumber = documentNumber,
            transferredBy = transferredBy,
            acceptedBy = acceptedBy,
            movementType = MovementType.RETURN
        )
    }

    fun writeOffEquipment(
        inventoryId: String,
        date: String,
        documentNumber: String,
        reason: String,
        transferredBy: String,
        acceptedBy: String
    ) {
        for (index in items.indices) {
            val item = items[index]

            if (item.inventoryId == inventoryId) {
                if (item.status == EquipmentStatus.WRITTEN_OFF) {
                    println("Оборудование $inventoryId уже списано")
                    return
                }

                val oldPlace = item.currentGroup ?: "Склад"

                val movement = Movement(
                    movementType = MovementType.WRITE_OFF,
                    inventoryId = item.inventoryId,
                    typeName = item.typeName,
                    serialNumber = item.serialNumber,
                    quantity = item.quantity,
                    date = date,
                    documentNumber = documentNumber,
                    source = oldPlace,
                    destination = "Списание",
                    location = item.currentLocation,
                    transferredBy = transferredBy,
                    acceptedBy = acceptedBy,
                    completenessStatus = item.completenessStatus,
                    missingParts = reason
                )

                movements.add(movement)

                val updatedItem = item.copy(
                    status = EquipmentStatus.WRITTEN_OFF,
                    currentGroup = null,
                    currentLocation = null
                )

                items[index] = updatedItem

                println("Оборудование $inventoryId списано")
                return
            }
        }

        println("Оборудование с номером $inventoryId не найдено")
    }

    fun validateDataIntegrity(): Int {
        val existingInventoryIds = mutableSetOf<String>()
        var problemCount = 0

        for (item in items) {
            existingInventoryIds.add(item.inventoryId)
        }
        for (movement in movements) {
            if (!existingInventoryIds.contains(movement.inventoryId)) {
                problemCount++
                println("ВНИМАНИЕ: В истории есть движение по ${movement.inventoryId}, но такого оборудования нет в списке оборудования")
            }
        }
        return problemCount
    }

    fun editEquipment(
        inventoryId: String,
        newTypeName: String,
        newSerialNumber: String?,
        newCompletenessStatus: CompletenessStatus,
        newMissingParts: String?
    ): Boolean {
        for (index in items.indices) {
            val item = items[index]
            if (item.inventoryId == inventoryId) {
                val updatedItem = item.copy(
                    typeName = newTypeName,
                    serialNumber = newSerialNumber,
                    completenessStatus = newCompletenessStatus,
                    missingParts = newMissingParts
                )
                items[index] = updatedItem
                return true
            }
        }
        return false
    }
}