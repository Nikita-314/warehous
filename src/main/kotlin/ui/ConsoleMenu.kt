package ui

import excel.ExcelStorage
import service.EquipmentService
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ConsoleMenu(
    private val equipmentService: EquipmentService,
    private val storage: ExcelStorage
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun start() {
        while (true) {
            println()
            println("=== СКЛАД ===")
            println("1. Показать оборудование")
            println("2. Показать историю движения")
            println("3. Добавить оборудование")
            println("4. Передать оборудование")
            println("5. Найти оборудование")
            println("6. Вернуть на склад")
            println("7. Показать остатки")
            println("8. Списать оборудование")
            println("0. Выход")
            print("Выберите действие: ")

            val command = readln()

            when (command) {
                "1" -> showEquipment()
                "2" -> showMovements()
                "3" -> addEquipment()
                "4" -> transferEquipment()
                "5" -> findEquipment()
                "6" -> returnEquipment()
                "7" -> showStockBalance()
                "8" -> writeOffEquipment()
                "0" -> {
                    println("Выход из программы")
                    return
                }
                else -> println("Неизвестная команда")
            }
        }
    }
    private fun addEquipment() {
        println()
        print("Введите название оборудования: ")
        val typeName = readln()

        print("Введите серийный номер. Если номера нет, нажмите Enter: ")
        val serialInput = readln()

        val serialNumber =
            if (serialInput.isBlank()) null else serialInput

        print("Введите количество: ")
        val quantity = readln().toInt()

        equipmentService.addEquipment(
            typeName = typeName,
            serialNumber = serialNumber,
            quantity = quantity
        )

        storage.saveEquipment(
            equipmentService.getAllItems()
        )

        storage.saveMovements(
            equipmentService.getAllMovements()
        )

        storage.saveGroupSheets(
            items = equipmentService.getAllItems(),
            movements = equipmentService.getAllMovements()
        )

        println("Оборудование добавлено и Excel обновлён")
    }

    private fun showEquipment() {
        val items = equipmentService.getAllItems()

        println()
        println("Оборудование:")

        for (item in items) {
            val group = item.currentGroup ?: "Склад"
            val location = item.currentLocation ?: "-"

            println("${item.inventoryId} | ${item.typeName} | ${group} | ${location}")
        }
    }

    private fun showMovements() {
        val movements = equipmentService.getAllMovements()

        println()
        println("История движения:")

        for (movement in movements) {
            println("${movement.date} | ${movement.inventoryId} | ${movement.source} -> ${movement.destination} | ${movement.documentNumber}")
        }
    }

    private fun transferEquipment() {
        println()

        print("Введите инвентарный номер: ")
        val inventoryId = readln()

        print("Введите группу: ")
        val group = readln()

        print("Введите объект: ")
        val location = readln()

        print("Введите номер накладной: ")
        val documentNumber = readln()

        equipmentService.transferEquipment(
            inventoryId = inventoryId,
            newGroup = group,
            newLocation = location,
            date = LocalDate.now().format(dateFormatter),
            documentNumber = documentNumber,
            transferredBy = "Кладовщик",
            acceptedBy = "Ответственный"
        )

        storage.saveEquipment(
            equipmentService.getAllItems()
        )

        storage.saveMovements(
            equipmentService.getAllMovements()
        )

        storage.saveGroupSheets(
            items = equipmentService.getAllItems(),
            movements = equipmentService.getAllMovements()
        )

        println("Передача завершена")
    }

    private fun findEquipment() {
        println()

        print("Введите инвентарный номер: ")
        val inventoryId = readln()

        val foundItem = equipmentService.findEquipmentById(inventoryId)

        if (foundItem == null) {
            println("Оборудование с номером $inventoryId не найдено")
            return
        }

        val group = foundItem.currentGroup ?: "Склад"
        val location = foundItem.currentLocation ?: "-"

        println()
        println("Карточка оборудования:")
        println("Инвентарный номер: ${foundItem.inventoryId}")
        println("Название: ${foundItem.typeName}")
        println("Серийный номер: ${foundItem.serialNumber ?: "-"}")
        println("Количество: ${foundItem.quantity}")
        println("Группа: $group")
        println("Объект: $location")
        println("Комплектность: ${foundItem.completenessStatus.title}")
        println("Отсутствует: ${foundItem.missingParts ?: "-"}")
        val movementCount = equipmentService.getMovementCount(inventoryId)
        val lastMovement = equipmentService.getLastMovement(inventoryId)

        println("Количество перемещений: $movementCount")
        println("Последняя дата движения: ${lastMovement?.date ?: "-"}")
        println("Последняя накладная: ${lastMovement?.documentNumber ?: "-"}")


        println()
        println("История движения:")

        val movements = equipmentService.getAllMovements()

        for (movement in movements) {
            if (movement.inventoryId == inventoryId) {
                println("${movement.date} | ${movement.movementType.title} | ${movement.source} -> ${movement.destination} | ${movement.documentNumber}")
            }
        }
    }

    private fun returnEquipment() {
        println()

        print("Введите инвентарный номер: ")
        val inventoryId = readln()

        print("Введите номер накладной: ")
        val documentNumber = readln()

        equipmentService.returnEquipment(
            inventoryId = inventoryId,
            date = LocalDate.now().format(dateFormatter),
            documentNumber = documentNumber,
            transferredBy = "Ответственный",
            acceptedBy = "Кладовщик"
        )

        storage.saveEquipment(
            equipmentService.getAllItems()
        )

        storage.saveMovements(
            equipmentService.getAllMovements()
        )

        storage.saveGroupSheets(
            items = equipmentService.getAllItems(),
            movements = equipmentService.getAllMovements()
        )

        println("Возврат завершён")
    }

    private fun showStockBalance() {
        println()
        println("Остатки:")

        val items = equipmentService.getAllItems()

        val balance = mutableMapOf<String, MutableMap<String, Int>>()

        for (item in items) {
            if (item.status == model.EquipmentStatus.WRITTEN_OFF) {
                continue
            }
            val place = item.currentGroup ?: "Склад"
            val itemName = item.typeName

            val itemsInPlace =
                balance.getOrPut(place) {
                    mutableMapOf()
                }

            val currentQuantity =
                itemsInPlace[itemName] ?: 0

            itemsInPlace[itemName] =
                currentQuantity + item.quantity
        }

        for (place in balance.keys) {
            println()
            println(place)
            println("----------")

            val itemsInPlace = balance[place]

            if (itemsInPlace != null) {
                for (itemName in itemsInPlace.keys) {
                    val quantity = itemsInPlace[itemName]
                    println("$itemName: $quantity")
                }
            }
        }
    }

    private fun writeOffEquipment() {
        println()

        print("Введите инвентарный номер: ")
        val inventoryId = readln()

        print("Введите номер акта/накладной списания: ")
        val documentNumber = readln()

        print("Введите причину списания: ")
        val reason = readln()

        equipmentService.writeOffEquipment(
            inventoryId = inventoryId,
            date = LocalDate.now().format(dateFormatter),
            documentNumber = documentNumber,
            reason = reason,
            transferredBy = "Кладовщик",
            acceptedBy = "Комиссия"
        )

        storage.saveEquipment(
            equipmentService.getAllItems()
        )

        storage.saveMovements(
            equipmentService.getAllMovements()
        )

        storage.saveGroupSheets(
            items = equipmentService.getAllItems(),
            movements = equipmentService.getAllMovements()
        )

        println("Списание завершено")
    }
}

