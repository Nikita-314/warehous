import model.EquipmentItem
import model.EquipmentStatus
import model.CompletenessStatus
import model.Movement
import model.AppSettings
import service.EquipmentCatalogService
import service.GroupCatalogService
import service.EquipmentService
import kotlin.time.Clock
import excel.ExcelStorage
import ui.ConsoleMenu


fun main() {
    val storage = ExcelStorage()

    val loadedSettings = storage.loadSettings()

    val settings =
        if (loadedSettings == null) {
            println("Первый запуск программы. Нужно настроить названия.")

            print("Как назвать главное место учёта? Например: Склад, Лаборатория: ")
            val warehouseTitle = readln()

            print("Как назвать учитываемые объекты? Например: Оборудование, Материалы: ")
            val equipmentTitle = readln()

            print("Как назвать получателей? Например: Группа, Техпомещение: ")
            val groupTitle = readln()

            print("Как назвать местоположение? Например: Объект, Город: ")
            val locationTitle = readln()

            val newSettings = AppSettings(
                warehouseTitle = warehouseTitle,
                equipmentTitle = equipmentTitle,
                groupTitle = groupTitle,
                locationTitle = locationTitle
            )

            storage.saveSettings(newSettings)

            newSettings
        } else {
            loadedSettings
        }

    val equipmentService = EquipmentService()

    equipmentService.loadInitialData(
        loadedItems = storage.loadEquipment(),
        loadedMovements = storage.loadMovements()
    )

    equipmentService.validateDataIntegrity()

    val menu = ConsoleMenu(
        equipmentService = equipmentService,
        storage = storage,
        settings = settings
    )

    menu.start()
}