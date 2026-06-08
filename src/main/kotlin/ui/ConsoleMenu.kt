package ui

import excel.ExcelStorage
import service.EquipmentService
import service.DocumentService
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import model.AppSettings

class ConsoleMenu(
    private val equipmentService: EquipmentService,
    private val storage: ExcelStorage,
    private var settings: AppSettings,
) {
    private val documentService = DocumentService()
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")


    fun start() {
        while (true) {
            println()
            println("=== ${settings.warehouseTitle.uppercase()} ===")
            println("1. Показать ${settings.equipmentTitle.lowercase()}")
            println("2. Показать историю движения")
            println("3. Добавить ${settings.equipmentTitle.lowercase()}")
            println("4. Передать ${settings.equipmentTitle.lowercase()}")
            println("5. Найти ${settings.equipmentTitle.lowercase()}")
            println("6. Вернуть ${settings.equipmentTitle.lowercase()} на ${settings.warehouseTitle.lowercase()}")
            println("7. Показать остатки")
            println("8. Списать ${settings.equipmentTitle.lowercase()}")
            println("9. Проверить целостность данных")
            println("10. Редактировать ${settings.equipmentTitle.lowercase()}")
            println("11. Удалить ошибочно созданное ${settings.equipmentTitle.lowercase()}")
            println("12. Поиск по названию")
            println("13. Показать ${settings.equipmentTitle.lowercase()} ${settings.groupTitle.lowercase()}")
            println("14. Восстановить ${settings.equipmentTitle.lowercase()} из истории")
            println("15. Управление настройками")
            println("16. Управление подразделениями")
            println("17. Управление объектами")
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
                "9" -> validateData()
                "10" -> editEquipment()
                "11" -> deleteEquipment()
                "12" -> searchByName()
                "13" -> showGroupEquipment()
                "14" -> restoreEquipment()
                "15" -> editSettings()
                "16" -> manageGroups()
                "17" -> manageLocations()


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

        saveAll()

        println("Оборудование добавлено и Excel обновлён")
    }

    private fun showEquipment() {
        val items = equipmentService.getAllItems()
        println("Всего записей: ${items.size}")

        println()
        println("Оборудование:")

        for (item in items) {
            val group = item.currentGroup ?: "Склад"
            val location = item.currentLocation ?: "-"

            println(
                "${item.inventoryId} | " +
                        "${item.typeName} | " +
                        "${item.quantity} шт. | " +
                        "$group | " +
                        location
            )
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

        val inventoryId = selectEquipmentForOperation()

        if (inventoryId == null) {
            println("Передача отменена")
            return
        }

        val group = selectGroup()

        if (group == null) {
            println("Передача отменена")
            return
        }

        val location = selectLocation()

        print("Введите количество для передачи: ")
        val quantity = readln().toInt()

        print("Введите номер накладной: ")
        val documentNumber = readln()

        print("Кто передал: ")
        val transferredBy = readln()

        print("Кто принял: ")
        val acceptedBy = readln()

        val movement = equipmentService.transferEquipment(
            inventoryId = inventoryId,
            newGroup = group,
            newLocation = location,
            transferQuantity = quantity,
            date = LocalDate.now().format(dateFormatter),
            documentNumber = documentNumber,
            transferredBy = transferredBy,
            acceptedBy = acceptedBy
        )

        if (movement != null) {

            if (documentNumber.isNotBlank()) {
                documentService.createTransferDocument(movement)
            }

            saveAll()
            println("Передача завершена")

        } else {
            println("Передача не выполнена")
        }
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
        println("${settings.groupTitle}: $group")
        println("${settings.locationTitle}: $location")
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

            if (movement.inventoryId != inventoryId) {
                continue
            }

            println(
                "${movement.date} | " +
                        "${movement.movementType.title} | " +
                        "${movement.source} -> ${movement.destination} | " +
                        movement.documentNumber
            )

            println(
                "Передал: ${movement.transferredBy} | " +
                        "Принял: ${movement.acceptedBy}"
            )

            println()
        }
    }

    private fun returnEquipment() {
        println()

        print("Введите инвентарный номер: ")
        val inventoryId = readln()

        print("Введите номер накладной: ")
        val documentNumber = readln()

        print("Кто передал: ")
        val transferredBy = readln()

        print("Кто принял: ")
        val acceptedBy = readln()

        equipmentService.returnEquipment(
            inventoryId = inventoryId,
            date = LocalDate.now().format(dateFormatter),
            documentNumber = documentNumber,
            transferredBy = transferredBy,
            acceptedBy = acceptedBy
        )

        saveAll()

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

        saveAll()

        println("Списание завершено")
    }

    private fun validateData() {
        println()
        println("Проверка целостности данных:")
        val problemCount = equipmentService.validateDataIntegrity()
        println("Проверка завершена. Найдено проблем: $problemCount")
    }

    private fun editEquipment() {
        println()

        print("Введите инвентарный номер: ")
        val inventoryId = readln()

        val item = equipmentService.findEquipmentById(inventoryId)

        if (item == null) {
            println("Оборудование с номером $inventoryId не найдено")
            return
        }

        println("Текущее название: ${item.typeName}")
        print("Новое название. Если не менять, нажмите Enter: ")
        val typeNameInput = readln()

        val newTypeName =
            if (typeNameInput.isBlank()) item.typeName else typeNameInput

        println("Текущий серийный номер: ${item.serialNumber ?: "-"}")
        print("Новый серийный номер. Если не менять, нажмите Enter: ")
        val serialInput = readln()

        val newSerialNumber =
            if (serialInput.isBlank()) item.serialNumber else serialInput

        println("Текущая комплектность: ${item.completenessStatus.title}")
        println("1. Комплект")
        println("2. Некомплект")
        print("Выберите комплектность. Если не менять, нажмите Enter: ")
        val completenessInput = readln()

        val newCompletenessStatus =
            when (completenessInput) {
                "1" -> model.CompletenessStatus.COMPLETE
                "2" -> model.CompletenessStatus.INCOMPLETE
                else -> item.completenessStatus
            }

        println("Сейчас отсутствует: ${item.missingParts ?: "-"}")
        print("Что отсутствует. Если не менять, нажмите Enter: ")
        val missingPartsInput = readln()

        val newMissingParts =
            if (missingPartsInput.isBlank()) item.missingParts else missingPartsInput

        val success = equipmentService.editEquipment(
            inventoryId = inventoryId,
            newTypeName = newTypeName,
            newSerialNumber = newSerialNumber,
            newCompletenessStatus = newCompletenessStatus,
            newMissingParts = newMissingParts
        )

        if (success) {

            saveAll()

            println("Оборудование отредактировано")

        } else {

            println("Не удалось отредактировать оборудование")

        }
    }

    private fun deleteEquipment() {
        println()

        print("Введите инвентарный номер: ")
        val inventoryId = readln()

        println("ВНИМАНИЕ: удаление разрешено только если по оборудованию нет истории движения.")
        print("Для подтверждения введите ДА: ")
        val confirm = readln()

        if (!confirm.equals("ДА", ignoreCase = true)) {
            println("Удаление отменено")
            return
        }

        val success =
            equipmentService.deleteEquipmentIfNoMovements(inventoryId)

        if (success) {
            saveAll()
        }
    }

    private fun saveAll() {
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
        storage.saveDepartmentNames(
            equipmentService.getAllGroups()
        )
        storage.saveLocationNames(
            equipmentService.getAllLocations()
        )
    }

    private fun searchByName() {
        println()

        print("Введите часть названия: ")
        val query = readln()

        val items = equipmentService.getAllItems()

        println()
        println("Результаты поиска:")

        var foundCount = 0

        for (item in items) {
            if (item.typeName.contains(query, ignoreCase = true)) {
                val group = item.currentGroup ?: "Склад"
                val location = item.currentLocation ?: "-"

                println("${item.inventoryId} | ${item.typeName} | ${group} | ${location}")

                foundCount++
            }
        }

        if (foundCount == 0) {
            println("Ничего не найдено")
        }
    }

    private fun showGroupEquipment() {
        println()

        print("Введите группу: ")
        val groupName = readln()

        val items = equipmentService.getAllItems()

        println()
        println("Оборудование группы $groupName:")

        var foundCount = 0

        for (item in items) {

            if (item.currentGroup == groupName) {

                println(
                    "${item.inventoryId} | ${item.typeName} | ${item.currentLocation ?: "-"}"
                )

                foundCount++
            }
        }

        if (foundCount == 0) {
            println("Оборудование не найдено")
        }
    }

    private fun restoreEquipment() {
        println()

        print("Введите инвентарный номер для восстановления: ")
        val inventoryId = readln()

        val success =
            equipmentService.restoreEquipmentFromHistory(inventoryId)

        if (success) {
            saveAll()
            println("Восстановление завершено")
        } else {
            println("Восстановление не выполнено")
        }
    }

    private fun editSettings() {
        println()

        println("Текущие настройки:")
        println("1. Главное место учёта: ${settings.warehouseTitle}")
        println("2. Учитываемые объекты: ${settings.equipmentTitle}")
        println("3. Получатели: ${settings.groupTitle}")
        println("4. Местоположение: ${settings.locationTitle}")

        print("Новое название главного места учёта. Если не менять, нажмите Enter: ")
        val warehouseInput = readln()

        print("Новое название учитываемых объектов. Если не менять, нажмите Enter: ")
        val equipmentInput = readln()

        print("Новое название получателей. Если не менять, нажмите Enter: ")
        val groupInput = readln()

        print("Новое название местоположения. Если не менять, нажмите Enter: ")
        val locationInput = readln()

        val newSettings = AppSettings(
            warehouseTitle = if (warehouseInput.isBlank()) settings.warehouseTitle else warehouseInput,
            equipmentTitle = if (equipmentInput.isBlank()) settings.equipmentTitle else equipmentInput,
            groupTitle = if (groupInput.isBlank()) settings.groupTitle else groupInput,
            locationTitle = if (locationInput.isBlank()) settings.locationTitle else locationInput
        )

        storage.saveSettings(newSettings)

        settings = newSettings

        println("Настройки сохранены")
    }

    private fun manageGroups() {
        while (true) {
            println()
            println("=== Подразделения ===")

            val groups = equipmentService.getAllGroups()

            if (groups.isEmpty()) {
                println("Справочник пуст")
            } else {
                println("Список подразделений:")

                for ((index, group) in groups.withIndex()) {
                    println("${index + 1}. $group")
                }
            }

            println()
            println("1. Добавить подразделение")
            println("2. Удалить подразделение")
            println("0. Назад")
            print("Выберите действие: ")

            when (readln()) {
                "1" -> {
                    print("Введите название нового подразделения: ")
                    val newGroup = readln()
                    val success = equipmentService.addGroup(newGroup)

                    if (success) {
                        saveAll()
                    }
                }

                "2" -> {
                    print("Введите название подразделения для удаления: ")
                    val groupName = readln()
                    val success = equipmentService.deleteGroup(groupName)

                    if (success) {
                        saveAll()
                    }
                }

                "0" -> return

                else -> println("Неизвестная команда")
            }
        }
    }

    private fun selectGroup(): String? {
        val groups = equipmentService.getAllGroups()

        if (groups.isEmpty()) {
            println("Справочник подразделений пуст")
            return null
        }

        println("Выберите подразделение:")

        for ((index, group) in groups.withIndex()) {
            println("${index + 1}. $group")
        }

        print("Введите номер подразделения: ")

        val input = readln()
        val number = input.toIntOrNull()

        if (number == null) {
            println("Нужно ввести число")
            return null
        }

        if (number < 1 || number > groups.size) {
            println("Нет подразделения с таким номером")
            return null
        }

        return groups[number - 1]
    }

    private fun manageLocations() {
        while (true) {
            println()
            println("=== Объекты ===")

            val locations = equipmentService.getAllLocations()

            if (locations.isEmpty()) {
                println("Справочник пуст")
            } else {
                println("Список объектов:")

                for ((index, location) in locations.withIndex()) {
                    println("${index + 1}. $location")
                }
            }

            println()
            println("1. Добавить объект")
            println("2. Удалить объект")
            println("0. Назад")
            print("Выберите действие: ")

            when (readln()) {
                "1" -> {
                    print("Введите название нового объекта: ")
                    val name = readln()

                    val success = equipmentService.addLocation(name)

                    if (success) {
                        saveAll()
                    }
                }

                "2" -> {
                    print("Введите название объекта для удаления: ")
                    val name = readln()

                    val success = equipmentService.deleteLocation(name)

                    if (success) {
                        saveAll()
                    }
                }

                "0" -> return

                else -> println("Неизвестная команда")
            }
        }
    }

    private fun selectLocation(): String? {
        val locations = equipmentService.getAllLocations()

        if (locations.isEmpty()) {
            println("Справочник объектов пуст")
            println("Объект не будет указан")
            return null
        }

        println("Выберите объект:")
        println("0. Не указывать объект")

        for ((index, location) in locations.withIndex()) {
            println("${index + 1}. $location")
        }

        print("Введите номер объекта: ")

        val input = readln()
        val number = input.toIntOrNull()

        if (number == null) {
            println("Нужно ввести число")
            return null
        }

        if (number == 0) {
            return null
        }

        if (number < 1 || number > locations.size) {
            println("Нет объекта с таким номером")
            return null
        }

        return locations[number - 1]
    }

    private fun selectEquipmentForOperation(): String? {
        println()

        print("Введите инвентарный номер, серийный номер или часть названия: ")
        val query = readln()

        if (query.isBlank()) {
            println("Поиск не может быть пустым")
            return null
        }

        val items = equipmentService.getAllItems()
        val foundItems = mutableListOf<model.EquipmentItem>()

        for (item in items) {
            val inventoryMatches =
                item.inventoryId.equals(query, ignoreCase = true)

            val serialMatches =
                item.serialNumber?.equals(query, ignoreCase = true) ?: false

            val nameMatches =
                item.typeName.contains(query, ignoreCase = true)

            if (inventoryMatches || serialMatches || nameMatches) {
                foundItems.add(item)
            }
        }

        if (foundItems.isEmpty()) {
            println("Ничего не найдено")
            return null
        }

        if (foundItems.size == 1) {
            val item = foundItems[0]
            println("Найдено: ${item.inventoryId} | ${item.typeName} | ${item.quantity} шт.")
            return item.inventoryId
        }

        println("Найдено несколько вариантов:")

        for ((index, item) in foundItems.withIndex()) {
            val group = item.currentGroup ?: settings.warehouseTitle
            val location = item.currentLocation ?: "-"

            println(
                "${index + 1}. ${item.inventoryId} | " +
                        "${item.typeName} | " +
                        "${item.serialNumber ?: "-"} | " +
                        "${item.quantity} шт. | " +
                        "$group | $location"
            )
        }

        print("Выберите номер из списка: ")
        val selectedIndex = readln().toIntOrNull()

        if (selectedIndex == null) {
            println("Нужно ввести число")
            return null
        }

        if (selectedIndex < 1 || selectedIndex > foundItems.size) {
            println("Нет такого варианта")
            return null
        }

        return foundItems[selectedIndex - 1].inventoryId
    }
}

