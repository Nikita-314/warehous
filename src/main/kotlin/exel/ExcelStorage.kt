package excel

import model.EquipmentType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import model.Group

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
}