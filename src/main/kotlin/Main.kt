import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import excel.ExcelStorage
import model.EquipmentItem

fun main() = application {
    val storage = ExcelStorage()

    val loadedEquipment: List<EquipmentItem> =
        storage.loadEquipment()

    val movements =
        storage.loadMovements()

    val items: List<EquipmentRow> =
        loadedEquipment.map { item ->
            val lastMovement =
                movements.lastOrNull { movement ->
                    movement.inventoryId == item.inventoryId
                }

            EquipmentRow(
                inventoryId = item.inventoryId,
                name = item.typeName,
                quantity = item.quantity,
                group = item.currentGroup ?: "Склад",
                location = item.currentLocation ?: "-",
                lastMovementDate = lastMovement?.date ?: "-",
                lastDocumentNumber = lastMovement?.documentNumber ?: "-",
                lastTransferredBy = lastMovement?.transferredBy ?: "-",
                lastAcceptedBy = lastMovement?.acceptedBy ?: "-",
                lastSource = lastMovement?.source ?: "-"
            )
        }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Складской учёт"
    ) {
        App(items)
    }
}

data class EquipmentRow(
    val inventoryId: String,
    val name: String,
    val quantity: Int,
    val group: String,
    val location: String,
    val lastMovementDate: String,
    val lastDocumentNumber: String,
    val lastTransferredBy: String,
    val lastAcceptedBy: String,
    val lastSource: String
)

@Composable
@Preview
fun App(items: List<EquipmentRow>) {
    MaterialTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.width(220.dp)
            ) {
                Button(onClick = {}) {
                    Text("Оборудование")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {}) {
                    Text("Движение")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {}) {
                    Text("Остатки")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {}) {
                    Text("Справочники")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(onClick = {}) {
                    Text("Настройки")
                }
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                var searchText by remember { mutableStateOf("") }
                var selectedItem by remember { mutableStateOf<EquipmentRow?>(null) }
                var message by remember { mutableStateOf("") }

                TextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Поиск по номеру, названию или подразделению") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = {}) {
                    Text("+ Добавить")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Оборудование")

                Spacer(modifier = Modifier.height(12.dp))

                val filteredItems =
                    items.filter { item ->
                        item.inventoryId.contains(searchText, ignoreCase = true) ||
                                item.name.contains(searchText, ignoreCase = true) ||
                                item.group.contains(searchText, ignoreCase = true) ||
                                item.location.contains(searchText, ignoreCase = true)
                    }

                Row {
                    Text("Инв.№", modifier = Modifier.width(120.dp))
                    Text("Название", modifier = Modifier.width(220.dp))
                    Text("Кол-во", modifier = Modifier.width(80.dp))
                    Text("Подразделение", modifier = Modifier.width(140.dp))
                    Text("Объект", modifier = Modifier.width(120.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                for (item in filteredItems) {
                    Button(
                        onClick = {
                            selectedItem = item
                        }
                    ) {
                        Row {
                            Text(item.inventoryId, modifier = Modifier.width(120.dp))
                            Text(item.name, modifier = Modifier.width(220.dp))
                            Text(item.quantity.toString(), modifier = Modifier.width(80.dp))
                            Text(item.group, modifier = Modifier.width(140.dp))
                            Text(item.location, modifier = Modifier.width(120.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (message.isNotBlank()) {
                    Text(message)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedItem != null) {
                    val item = selectedItem!!

                    Text("Карточка оборудования")

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Инвентарный номер: ${item.inventoryId}")
                    Text("Название: ${item.name}")
                    Text("Количество: ${item.quantity}")
                    Text("Подразделение: ${item.group}")
                    Text("Объект: ${item.location}")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Последнее движение")
                    Text("Дата: ${item.lastMovementDate}")
                    Text("Накладная: ${item.lastDocumentNumber}")
                    Text("Передал: ${if (item.lastTransferredBy.isBlank()) "-" else item.lastTransferredBy} (${item.lastSource})")
                    Text("Принял: ${if (item.lastAcceptedBy.isBlank()) "-" else item.lastAcceptedBy}")

                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
                        Button(
                            onClick = {
                                message = "Нажата передача для ${item.inventoryId}"
                            }
                        ) {
                            Text("Передать")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = {}) {
                            Text("Вернуть")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(onClick = {}) {
                            Text("История")
                        }

                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (message.isNotBlank()) {
                        Text(message)
                    }
                }
            }
        }
    }
}