package excel


import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import model.Group
import model.EquipmentItem
import model.EquipmentType
import model.EquipmentStatus
import model.CompletenessStatus
import model.Movement
import model.MovementType
import model.AppSettings


class ExcelStorage(
    private val fileName: String = "warehouse.xlsx"
) {

    fun saveEquipmentTypes(types: List<EquipmentType>) {
        val file = File(fileName)

        val workbook =
            if (file.exists()) {
                val inputStream = FileInputStream(file)
                XSSFWorkbook(inputStream)
            } else {
                XSSFWorkbook()
            }

        val sheetName = "Изделия"

        val oldSheet = workbook.getSheet(sheetName)
        if (oldSheet != null) {
            val index = workbook.getSheetIndex(oldSheet)
            workbook.removeSheetAt(index)
        }

        val sheet = workbook.createSheet(sheetName)

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Название изделия")

        for (index in types.indices) {
            val type = types[index]

            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(type.name)
        }

        val outputStream = FileOutputStream(file)
        workbook.write(outputStream)

        outputStream.close()
        workbook.close()
    }

    fun loadGroups(): List<Group> {

        val result = mutableListOf<Group>()

        val file = File(fileName)

        if (!file.exists()) {
            return result
        }

        val inputStream = FileInputStream(file)

        val workbook = XSSFWorkbook(inputStream)

        val sheet = workbook.getSheet("Группы")

        if (sheet == null) {
            workbook.close()
            return result
        }

        for (rowIndex in 1..sheet.lastRowNum) {

            val row = sheet.getRow(rowIndex)

            val name = row.getCell(0).stringCellValue

            result.add(
                Group(name)
            )
        }

        workbook.close()

        return result
    }

    fun loadEquipmentTypes(): List<EquipmentType> {
        val result = mutableListOf<EquipmentType>()
        val file = File(fileName)

        if (!file.exists()) {
            return result
        }

        val inputStream = FileInputStream(file)
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheet("Изделия")

        if (sheet == null) {
            workbook.close()
            return result
        }

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex)
            val name = row.getCell(0).stringCellValue
            result.add(EquipmentType(name))
        }
        workbook.close()
        return result
    }

    fun saveEquipment(items: List<EquipmentItem>) {
        val file = File(fileName)

        val workbook =
            if (file.exists()) {
                val inputStream = FileInputStream(file)
                XSSFWorkbook(inputStream)
            } else {
                XSSFWorkbook()
            }

        val sheetName = "Оборудование"

        val oldSheet = workbook.getSheet(sheetName)
        if (oldSheet != null) {
            val index = workbook.getSheetIndex(oldSheet)
            workbook.removeSheetAt(index)
        }

        val sheet = workbook.createSheet(sheetName)

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Инвентарный номер")
        headerRow.createCell(1).setCellValue("Название")
        headerRow.createCell(2).setCellValue("Серийный номер")
        headerRow.createCell(3).setCellValue("Количество")
        headerRow.createCell(4).setCellValue("Статус")
        headerRow.createCell(5).setCellValue("Группа")
        headerRow.createCell(6).setCellValue("Объект")
        headerRow.createCell(7).setCellValue("Комплектность")
        headerRow.createCell(8).setCellValue("Отсутствует")

        for (index in items.indices) {
            val item = items[index]
            val row = sheet.createRow(index + 1)

            row.createCell(0).setCellValue(item.inventoryId)
            row.createCell(1).setCellValue(item.typeName)
            row.createCell(2).setCellValue(item.serialNumber ?: "")
            row.createCell(3).setCellValue(item.quantity.toDouble())
            row.createCell(4).setCellValue(item.status.title)
            row.createCell(5).setCellValue(item.currentGroup ?: "")
            row.createCell(6).setCellValue(item.currentLocation ?: "")
            row.createCell(7).setCellValue(item.completenessStatus.title)
            row.createCell(8).setCellValue(item.missingParts ?: "")
        }

        val outputStream = FileOutputStream(file)
        workbook.write(outputStream)

        outputStream.close()
        workbook.close()
    }

    fun saveMovements(movements: List<Movement>) {
        val file = File(fileName)

        val workbook =
            if (file.exists()) {
                val inputStream = FileInputStream(file)
                XSSFWorkbook(inputStream)
            } else {
                XSSFWorkbook()
            }

        val sheetName = "Движение"

        val oldSheet = workbook.getSheet(sheetName)
        if (oldSheet != null) {
            val index = workbook.getSheetIndex(oldSheet)
            workbook.removeSheetAt(index)
        }

        val sheet = workbook.createSheet(sheetName)

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Тип движения")
        headerRow.createCell(1).setCellValue("Дата")
        headerRow.createCell(2).setCellValue("Накладная")
        headerRow.createCell(3).setCellValue("Инвентарный номер")
        headerRow.createCell(4).setCellValue("Название")
        headerRow.createCell(5).setCellValue("Серийный номер")
        headerRow.createCell(6).setCellValue("Количество")
        headerRow.createCell(7).setCellValue("Источник")
        headerRow.createCell(8).setCellValue("Получатель")
        headerRow.createCell(9).setCellValue("Объект")
        headerRow.createCell(10).setCellValue("Передал")
        headerRow.createCell(11).setCellValue("Принял")
        headerRow.createCell(12).setCellValue("Комплектность")
        headerRow.createCell(13).setCellValue("Отсутствует")

        for (index in movements.indices) {
            val movement = movements[index]
            val row = sheet.createRow(index + 1)

            row.createCell(0).setCellValue(movement.movementType.title)
            row.createCell(1).setCellValue(movement.date)
            row.createCell(2).setCellValue(movement.documentNumber)
            row.createCell(3).setCellValue(movement.inventoryId)
            row.createCell(4).setCellValue(movement.typeName)
            row.createCell(5).setCellValue(movement.serialNumber ?: "")
            row.createCell(6).setCellValue(movement.quantity.toDouble())
            row.createCell(7).setCellValue(movement.source)
            row.createCell(8).setCellValue(movement.destination)
            row.createCell(9).setCellValue(movement.location ?: "")
            row.createCell(10).setCellValue(movement.transferredBy)
            row.createCell(11).setCellValue(movement.acceptedBy)
            row.createCell(12).setCellValue(movement.completenessStatus.title)
            row.createCell(13).setCellValue(movement.missingParts ?: "")
        }

        val outputStream = FileOutputStream(file)
        workbook.write(outputStream)

        outputStream.close()
        workbook.close()
    }

    fun loadEquipment(): List<EquipmentItem> {
        val result = mutableListOf<EquipmentItem>()

        val file = File(fileName)

        if (!file.exists()) {
            return result
        }

        val inputStream = FileInputStream(file)
        val workbook = XSSFWorkbook(inputStream)

        val sheet = workbook.getSheet("Оборудование")

        if (sheet == null) {
            workbook.close()
            inputStream.close()
            return result
        }

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex)
            if (row == null) {
                continue
            }

            val inventoryCell = row.getCell(0)

            if (inventoryCell == null) {
                continue
            }
            val inventoryId = row.getCell(0).stringCellValue

            if (inventoryId.isBlank()) {
                continue
            }
            
            val typeName = readString(row, 1)
            val serialNumberText = readString(row, 2)
            val quantity = readInt(row, 3)
            val statusText = readString(row, 4)
            val currentGroupText = readString(row, 5)
            val currentLocationText = readString(row, 6)
            val completenessText = readString(row, 7)
            val missingPartsText = readString(row, 8)

            val item = EquipmentItem(
                inventoryId = inventoryId,
                typeName = typeName,
                serialNumber = if (serialNumberText.isBlank()) null else serialNumberText,
                quantity = quantity,
                status = if (statusText == "На складе") EquipmentStatus.IN_STOCK else EquipmentStatus.IN_GROUP,
                currentGroup = if (currentGroupText.isBlank()) null else currentGroupText,
                currentLocation = if (currentLocationText.isBlank()) null else currentLocationText,
                completenessStatus = if (completenessText == "Комплект") CompletenessStatus.COMPLETE else CompletenessStatus.INCOMPLETE,
                missingParts = if (missingPartsText.isBlank()) null else missingPartsText
            )

            result.add(item)
        }

        workbook.close()
        inputStream.close()

        return result
    }

    fun loadMovements(): List<Movement> {
        val result = mutableListOf<Movement>()

        val file = File(fileName)

        if (!file.exists()) {
            return result
        }

        val inputStream = FileInputStream(file)
        val workbook = XSSFWorkbook(inputStream)

        val sheet = workbook.getSheet("Движение")

        if (sheet == null) {
            workbook.close()
            inputStream.close()
            return result
        }

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex)

            val movementTypeText = row.getCell(0).stringCellValue
            val date = row.getCell(1).stringCellValue
            val documentNumber = row.getCell(2).stringCellValue
            val inventoryId = row.getCell(3).stringCellValue
            val typeName = row.getCell(4).stringCellValue
            val serialNumberText = row.getCell(5).stringCellValue
            val quantity = row.getCell(6).numericCellValue.toInt()
            val source = row.getCell(7).stringCellValue
            val destination = row.getCell(8).stringCellValue
            val locationText = row.getCell(9).stringCellValue
            val transferredBy = row.getCell(10).stringCellValue
            val acceptedBy = row.getCell(11).stringCellValue
            val completenessText = row.getCell(12).stringCellValue
            val missingPartsText = row.getCell(13).stringCellValue

            val movement = Movement(
                movementType = when (movementTypeText) {
                    "Поступление" -> MovementType.RECEIPT
                    "Передача" -> MovementType.TRANSFER
                    "Возврат" -> MovementType.RETURN
                    "Списание" -> MovementType.WRITE_OFF
                    else -> MovementType.TRANSFER
                },
                inventoryId = inventoryId,
                typeName = typeName,
                serialNumber = if (serialNumberText.isBlank()) null else serialNumberText,
                quantity = quantity,
                date = date,
                documentNumber = documentNumber,
                source = source,
                destination = destination,
                location = if (locationText.isBlank()) null else locationText,
                transferredBy = transferredBy,
                acceptedBy = acceptedBy,
                completenessStatus = if (completenessText == "Комплект") CompletenessStatus.COMPLETE else CompletenessStatus.INCOMPLETE,
                missingParts = if (missingPartsText.isBlank()) null else missingPartsText
            )

            result.add(movement)
        }

        workbook.close()
        inputStream.close()

        return result
    }

    fun saveGroupSheets(items: List<EquipmentItem>, movements: List<Movement>) {
        val file = File(fileName)

        val workbook =
            if (file.exists()) {
                val inputStream = FileInputStream(file)
                XSSFWorkbook(inputStream)
            } else {
                XSSFWorkbook()
            }

        val groupNames = mutableSetOf<String>()

        for (item in items) {
            val groupName = item.currentGroup ?: "Склад"
            groupNames.add(groupName)
        }

        for (groupName in groupNames) {
            val oldSheet = workbook.getSheet(groupName)
            if (oldSheet != null) {
                val index = workbook.getSheetIndex(oldSheet)
                workbook.removeSheetAt(index)
            }

            val sheet = workbook.createSheet(groupName)

            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Инвентарный номер")
            headerRow.createCell(1).setCellValue("Название")
            headerRow.createCell(2).setCellValue("Серийный номер")
            headerRow.createCell(3).setCellValue("Количество")
            headerRow.createCell(4).setCellValue("Объект")
            headerRow.createCell(5).setCellValue("Комплектность")
            headerRow.createCell(6).setCellValue("Отсутствует")
            headerRow.createCell(7).setCellValue("Дата получения")
            headerRow.createCell(8).setCellValue("Накладная")

            var rowIndex = 1

            for (item in items) {
                val itemGroupName = item.currentGroup ?: "Склад"

                if (itemGroupName == groupName) {
                    val row = sheet.createRow(rowIndex)

                    row.createCell(0).setCellValue(item.inventoryId)
                    row.createCell(1).setCellValue(item.typeName)
                    row.createCell(2).setCellValue(item.serialNumber ?: "")
                    row.createCell(3).setCellValue(item.quantity.toDouble())
                    row.createCell(4).setCellValue(item.currentLocation ?: "")
                    row.createCell(5).setCellValue(item.completenessStatus.title)
                    row.createCell(6).setCellValue(item.missingParts ?: "")
                    val lastMovement = findLastMovementForItem(item.inventoryId, movements)
                    row.createCell(7).setCellValue(lastMovement?.date ?: "")
                    row.createCell(8).setCellValue(lastMovement?.documentNumber ?: "")

                    rowIndex++
                }
            }
        }

        val outputStream = FileOutputStream(file)
        workbook.write(outputStream)

        outputStream.close()
        workbook.close()
    }

    private fun findLastMovementForItem(
        inventoryId: String,
        movements: List<Movement>
    ): Movement? {
        var lastMovement: Movement? = null

        for (movement in movements) {
            if (movement.inventoryId == inventoryId) {
                lastMovement = movement
            }
        }

        return lastMovement
    }

    private fun readString(
        row: org.apache.poi.ss.usermodel.Row,
        // cellIndex — номер ячейки
        cellIndex: Int
    ) : String {
        val cell = row.getCell(cellIndex)
        if (cell == null) {
            return ""
        }
        return cell.toString()
    }

    private fun readInt(
        row: org.apache.poi.ss.usermodel.Row,
        cellIndex: Int
    ): Int {
        val cell = row.getCell(cellIndex)
        if (cell == null) {
            return 0
        }
        // toDoubleOrNull() — попытаться превратить в число, если не получилось — вернуть null
        return cell.toString().toDoubleOrNull()?.toInt() ?: 0
    }

    fun saveSettings(settings: AppSettings) {
        val file = File(fileName)

        val workbook =
            if (file.exists()) {
                val inputStream = FileInputStream(file)
                XSSFWorkbook(inputStream)
            } else {
                XSSFWorkbook()
            }

        val sheetName = "Настройки"

        val oldSheet = workbook.getSheet(sheetName)
        if (oldSheet != null) {
            val index = workbook.getSheetIndex(oldSheet)
            workbook.removeSheetAt(index)
        }

        val sheet = workbook.createSheet(sheetName)

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Ключ")
        headerRow.createCell(1).setCellValue("Значение")

        sheet.createRow(1).apply {
            createCell(0).setCellValue("warehouseTitle")
            createCell(1).setCellValue(settings.warehouseTitle)
        }

        sheet.createRow(2).apply {
            createCell(0).setCellValue("equipmentTitle")
            createCell(1).setCellValue(settings.equipmentTitle)
        }

        sheet.createRow(3).apply {
            createCell(0).setCellValue("groupTitle")
            createCell(1).setCellValue(settings.groupTitle)
        }

        sheet.createRow(4).apply {
            createCell(0).setCellValue("locationTitle")
            createCell(1).setCellValue(settings.locationTitle)
        }

        val outputStream = FileOutputStream(file)
        workbook.write(outputStream)

        outputStream.close()
        workbook.close()
    }

    fun loadSettings(): AppSettings? {
        val file = File(fileName)

        if (!file.exists()) {
            return null
        }

        val inputStream = FileInputStream(file)
        val workbook = XSSFWorkbook(inputStream)

        val sheet = workbook.getSheet("Настройки")

        if (sheet == null) {
            workbook.close()
            inputStream.close()
            return null
        }

        var warehouseTitle = "Склад"
        var equipmentTitle = "Оборудование"
        var groupTitle = "Группа"
        var locationTitle = "Объект"

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex)

            if (row == null) {
                continue
            }

            val key = readString(row, 0)
            val value = readString(row, 1)

            when (key) {
                "warehouseTitle" -> warehouseTitle = value
                "equipmentTitle" -> equipmentTitle = value
                "groupTitle" -> groupTitle = value
                "locationTitle" -> locationTitle = value
            }
        }

        workbook.close()
        inputStream.close()

        return AppSettings(
            warehouseTitle = warehouseTitle,
            equipmentTitle = equipmentTitle,
            groupTitle = groupTitle,
            locationTitle = locationTitle
        )
    }
}