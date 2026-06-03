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
    private val groups = mutableListOf<String>()

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
        transferQuantity: Int,
        date: String,
        documentNumber: String,
        transferredBy: String,
        acceptedBy: String,
        movementType: MovementType = MovementType.TRANSFER
    ): Movement? {
        for (index in items.indices) {
            val item = items[index]

            if (item.inventoryId == inventoryId) {
                if (transferQuantity <= 0) {
                    println("Количество для передачи должно быть больше 0")
                    return null
                }

                if (transferQuantity > item.quantity) {
                    println("Нельзя передать $transferQuantity. Доступно только ${item.quantity}")
                    return null
                }

                val oldPlace = item.currentGroup ?: "Склад"
                val oldLocation = item.currentLocation

                if (
                    oldPlace == newGroup &&
                    oldLocation == newLocation
                ) {
                    println(
                        "Оборудование $inventoryId уже находится в группе $newGroup на объекте ${newLocation ?: "-"}"
                    )
                    return null
                }

                val movementInventoryId =
                    if (transferQuantity == item.quantity) {
                        item.inventoryId
                    } else {
                        generateInventoryId()
                    }

                val movement = Movement(
                    movementType = movementType,
                    inventoryId = movementInventoryId,
                    typeName = item.typeName,
                    serialNumber = item.serialNumber,
                    quantity = transferQuantity,
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

                if (transferQuantity == item.quantity) {
                    val updatedItem = item.copy(
                        status = EquipmentStatus.IN_GROUP,
                        currentGroup = newGroup,
                        currentLocation = newLocation
                    )

                    items[index] = updatedItem
                } else {
                    val remainingItem = item.copy(
                        quantity = item.quantity - transferQuantity
                    )

                    items[index] = remainingItem

                    val transferredItem = item.copy(
                        inventoryId = movementInventoryId,
                        quantity = transferQuantity,
                        status = EquipmentStatus.IN_GROUP,
                        currentGroup = newGroup,
                        currentLocation = newLocation
                    )

                    items.add(transferredItem)
                }

                if (movementType == MovementType.RETURN) {
                    println("Оборудование $movementInventoryId возвращено на склад")
                } else {
                    println("Оборудование $movementInventoryId передано в группу $newGroup")
                }

                return movement
            }
        }

        println("Оборудование с номером $inventoryId не найдено")
        return null
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
        val item = findEquipmentById(inventoryId)

        if (item == null) {
            println("Оборудование с номером $inventoryId не найдено")
            return
        }

        transferEquipment(
            inventoryId = inventoryId,
            newGroup = "Склад",
            newLocation = null,
            transferQuantity = item.quantity,
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
        var problemCount = 0

        val existingInventoryIds = mutableSetOf<String>()
        val duplicateInventoryIds = mutableSetOf<String>()

        for (item in items) {
            if (existingInventoryIds.contains(item.inventoryId)) {
                duplicateInventoryIds.add(item.inventoryId)
            } else {
                existingInventoryIds.add(item.inventoryId)
            }
        }

        for (duplicateId in duplicateInventoryIds) {
            problemCount++

            println(
                "ВНИМАНИЕ: Найден дубликат инвентарного номера $duplicateId"
            )
        }

        for (movement in movements) {
            if (!existingInventoryIds.contains(movement.inventoryId)) {
                problemCount++

                println(
                    "ВНИМАНИЕ: В истории есть движение по ${movement.inventoryId}, но такого оборудования нет в списке оборудования"
                )
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
    fun deleteEquipmentIfNoMovements(
        inventoryId: String
    ): Boolean {

        for (movement in movements) {
            if (movement.inventoryId == inventoryId) {
                println("Нельзя удалить $inventoryId: по нему уже есть история движения")
                return false
            }
        }

        for (index in items.indices) {
            val item = items[index]

            if (item.inventoryId == inventoryId) {
                items.removeAt(index)
                println("Оборудование $inventoryId удалено")
                return true
            }
        }

        println("Оборудование с номером $inventoryId не найдено")
        return false
    }

    fun restoreEquipmentFromHistory(
        inventoryId: String
    ): Boolean {

        for (item in items) {
            if (item.inventoryId == inventoryId) {
                println("Оборудование уже существует")
                return false
            }
        }

        var lastMovement: Movement? = null

        for (movement in movements) {
            if (movement.inventoryId == inventoryId) {
                lastMovement = movement
            }
        }

        if (lastMovement == null) {
            println("История для $inventoryId не найдена")
            return false
        }
        val restoredStatus =
            when (lastMovement.movementType) {
                MovementType.WRITE_OFF -> EquipmentStatus.WRITTEN_OFF
                else -> EquipmentStatus.IN_GROUP
            }

        val restoredGroup =
            when (lastMovement.movementType) {
                MovementType.WRITE_OFF -> null
                else -> lastMovement.destination
            }

        val restoredLocation =
            when (lastMovement.movementType) {
                MovementType.WRITE_OFF -> null
                else -> lastMovement.location
            }

        val restoredItem = EquipmentItem(
            inventoryId = lastMovement.inventoryId,
            typeName = lastMovement.typeName,
            serialNumber = lastMovement.serialNumber,
            quantity = lastMovement.quantity,
            status = restoredStatus,
            currentGroup = restoredGroup,
            currentLocation = restoredLocation,
            completenessStatus = lastMovement.completenessStatus,
            missingParts = lastMovement.missingParts
        )

        items.add(restoredItem)

        println("Оборудование $inventoryId восстановлено из истории")

        return true
    }

    fun addGroup(name: String): Boolean {
        if (name.isBlank()) {
            println("Название подразделения не может быть пустым")
            return false
        }

        for (group in groups) {
            if (group.equals(name, ignoreCase = true)) {
                println("Подразделение $name уже существует")
                return false
            }
        }

        groups.add(name)
        println("Подразделение $name добавлено")
        return true
    }

    fun getAllGroups(): List<String> {
        val result = mutableSetOf<String>()

        for (group in groups) {
            result.add(group)
        }

        for (item in items) {
            if (!item.currentGroup.isNullOrBlank()) {
                result.add(item.currentGroup)
            }
        }

        return result.toList()
    }

    fun deleteGroup(name: String): Boolean {
        for (item in items) {
            if (item.currentGroup.equals(name, ignoreCase = true)) {
                println("Нельзя удалить подразделение $name: за ним числится оборудование")
                return false
            }
        }

        val removed = groups.removeIf {
            it.equals(name, ignoreCase = true)
        }

        if (removed) {
            println("Подразделение $name удалено")
            return true
        }

        println("Подразделение $name не найдено")
        return false
    }

    fun loadGroups(loadedGroups: List<String>) {
        groups.clear()
        groups.addAll(loadedGroups)
    }
}