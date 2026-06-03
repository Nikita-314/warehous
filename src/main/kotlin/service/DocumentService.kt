package service

import model.Movement
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class DocumentService(
    private val templatePath: String = "templates/ТН форма.xlsx",
    private val outputDir: String = "documents"
) {

    fun createTransferDocument(
        movement: Movement
    ): File? {

        val templateFile = File(templatePath)

        if (!templateFile.exists()) {
            println("Шаблон накладной не найден: $templatePath")
            return null
        }

        val documentsDir = File(outputDir)

        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }

        val inputStream = FileInputStream(templateFile)
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)

        setText(sheet, "CL6", movement.date)
        setText(sheet, "W14", movement.documentNumber)

        setText(sheet, "R8", movement.source)
        setText(sheet, "R10", movement.destination)

        setText(sheet, "A22", movement.typeName)
        setText(sheet, "R22", movement.serialNumber ?: "")
        setText(sheet, "AD22", "шт")
        setText(sheet, "AY22", movement.quantity.toString())
        setText(sheet, "CP22", movement.missingParts ?: "")
        setText(sheet, "BE22", movement.quantity.toString())

        setText(sheet, "A35", movement.transferredBy)
        setText(sheet, "A41", movement.acceptedBy)


        val safeDocumentNumber =
            movement.documentNumber
                .replace("/", "_")
                .replace("\\", "_")
                .replace(" ", "_")

        val safeInventoryId =
            movement.inventoryId
                .replace("/", "_")
                .replace("\\", "_")
                .replace(" ", "_")

        val outputFile = File(
            documentsDir,
            "Накладная_${safeDocumentNumber}_${safeInventoryId}.xlsx"
        )

        val outputStream = FileOutputStream(outputFile)
        workbook.write(outputStream)

        outputStream.close()
        workbook.close()
        inputStream.close()

        println("Файл накладной создан: ${outputFile.path}")

        return outputFile
    }

    private fun setText(
        sheet: org.apache.poi.ss.usermodel.Sheet,
        cellAddress: String,
        value: String
    ) {
        val cellReference =
            org.apache.poi.ss.util.CellReference(cellAddress)

        var rowIndex = cellReference.row
        var colIndex = cellReference.col.toInt()

        for (i in 0 until sheet.numMergedRegions) {
            val region = sheet.getMergedRegion(i)

            if (
                rowIndex >= region.firstRow &&
                rowIndex <= region.lastRow &&
                colIndex >= region.firstColumn &&
                colIndex <= region.lastColumn
            ) {
                rowIndex = region.firstRow
                colIndex = region.firstColumn
                break
            }
        }

        val row =
            sheet.getRow(rowIndex)
                ?: sheet.createRow(rowIndex)

        val cell =
            row.getCell(colIndex)
                ?: row.createCell(colIndex)

        cell.setCellValue(value)
    }

    fun createDebugTemplateMap(): File? {
        val templateFile = File(templatePath)

        if (!templateFile.exists()) {
            println("Шаблон накладной не найден: $templatePath")
            return null
        }

        val documentsDir = File(outputDir)
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }

        val inputStream = FileInputStream(templateFile)
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)

        setText(sheet, "CL6", "DATE")
        setText(sheet, "AM14", "DOC_NUMBER")
        setText(sheet, "W14", "DOC_NUMBER_2")
        setText(sheet, "G8", "FROM")
        setText(sheet, "G10", "TO")
        setText(sheet, "A23", "NAME")
        setText(sheet, "V23", "SERIAL")
        setText(sheet, "AD23", "UNIT")
        setText(sheet, "AP23", "QTY_1")
        setText(sheet, "AV23", "QTY_2")
        setText(sheet, "BD23", "QTY_3")
        setText(sheet, "CP23", "COMMENT")

        val outputFile = File(documentsDir, "DEBUG_TEMPLATE_MAP.xlsx")

        val outputStream = FileOutputStream(outputFile)
        workbook.write(outputStream)

        outputStream.close()
        workbook.close()
        inputStream.close()

        println("Файл разметки шаблона создан: ${outputFile.path}")

        return outputFile
    }
}

