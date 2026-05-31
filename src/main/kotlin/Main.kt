import model.EquipmentItem
import model.EquipmentStatus
import model.CompletenessStatus
import model.Movement
import service.EquipmentCatalogService
import service.GroupCatalogService
import service.EquipmentService
import kotlin.time.Clock
import excel.ExcelStorage
import ui.ConsoleMenu


fun main() {
    val storage = ExcelStorage()
    val equipmentService = EquipmentService()

    equipmentService.loadInitialData(
        loadedItems = storage.loadEquipment(),
        loadedMovements = storage.loadMovements()
    )

    equipmentService.loadInitialData(
        loadedItems = storage.loadEquipment(),
        loadedMovements = storage.loadMovements()
    )
    equipmentService.validateDataIntegrity()

    val menu = ConsoleMenu(
        equipmentService = equipmentService,
        storage = storage
    )
    menu.start()

}