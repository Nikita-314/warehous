package service

import  model.EquipmentType

class EquipmentCatalogService {
    private val equipmentTypes = mutableListOf<EquipmentType>()
    fun loadInitialTypes(types: List<EquipmentType>) {
        // очищает старый список
        equipmentTypes.clear()
        // добавляет все изделия, которые пришли из Excel.
        equipmentTypes.addAll(types)
    }

    fun addEquipmentType(name: String) {
        for (type in equipmentTypes) {
            if (type.name.equals(name, ignoreCase = true)) {
                println("Тип изделия '$name' уже существует")
                return
            }
        }
        val equipmentType = EquipmentType(name)
        equipmentTypes.add(equipmentType)
    }
    fun getAllEquipmentTypes(): List<EquipmentType> {
        return equipmentTypes
    }
}