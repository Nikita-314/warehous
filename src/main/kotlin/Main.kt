import model.EquipmentItem
import model.EquipmentStatus
import model.CompletenessStatus
import model.Movement
import service.EquipmentCatalogService
import service.GroupCatalogService
import service.EquipmentService
import kotlin.time.Clock
import excel.ExcelStorage


fun main() {

    val equipmentService = EquipmentService()

    equipmentService.addEquipment(
        typeName = "Батарейка AA",
        serialNumber = null,
        quantity = 9
    )

    equipmentService.addEquipment(
        typeName = "Батарейка AA",
        serialNumber = "8776",
        quantity = 10
    )

    for (item in equipmentService.getAllItems()) {
        println(
            "${item.inventoryId} | ${item.typeName} | ${item.serialNumber} | ${item.quantity}"
        )
    }
}