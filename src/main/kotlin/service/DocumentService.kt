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
        setText(sheet, "AM14", movement.documentNumber)

        setText(sheet, "G8", movement.source)
        setText(sheet, "G10", movement.destination)

        setText(sheet, "A23", movement.typeName)
        setText(sheet, "V23", movement.serialNumber ?: "")
        setText(sheet, "AD23", "шт")
        setText(sheet, "AV23", movement.quantity.toString())
        setText(sheet, "BD23", movement.quantity.toString())
        setText(sheet, "CP23", movement.missingParts ?: "")

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
}

