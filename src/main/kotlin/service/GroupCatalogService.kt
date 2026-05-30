package service

import model.Group

class GroupCatalogService {

    private val groups = mutableListOf<Group>()

    fun addGroup(name: String) {

        for (group in groups) {

            if (group.name.equals(name, ignoreCase = true)) {
                println("Группа '$name' уже существует")
                return
            }
        }

        groups.add(
            Group(name)
        )
    }

    fun getAllGroups(): List<Group> {
        return groups
    }

    fun loadInitialGroups(groupsFromExcel: List<Group>) {
        groups.clear()
        groups.addAll(groupsFromExcel)
    }
}

