package hu.otilsoft.locations.dialog

import hu.otilsoft.locations.Category
import hu.otilsoft.locations.NoButtonBarDialogPane
import hu.otilsoft.locations.manager.ItemManager
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.ButtonType
import javafx.scene.control.Dialog
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.stage.Modality
import javafx.stage.Window
import java.io.File
import java.sql.Connection
import java.util.*


class ImportLocationsDialog(owner: Window, category: Category, itemManager: ItemManager, psv: File) : Dialog<ButtonType>() {
    @FXML private lateinit var progressImport: ProgressBar
    @FXML private lateinit var labelImport: Label

    init {
        val resources = ResourceBundle.getBundle("import-items-dialog")
        val fxmlLoader = FXMLLoader(this::class.java.getResource("/import-items-dialog.fxml"), resources)
        fxmlLoader.setController(this)

        initOwner(owner)
        initModality(Modality.APPLICATION_MODAL)

        dialogPane = NoButtonBarDialogPane()
        dialogPane.content = fxmlLoader.load()
        dialogPane.scene.window.setOnCloseRequest { _ -> run { onCloseRequest() } }

        title = resources.getString("DIALOG.TITLE")

        val task = Importer(itemManager.connection, category.id, psv, resources.getString("LABEL.IMPORT"), this)
        progressImport.progressProperty().bind(task.progressProperty())
        labelImport.textProperty().bind(task.messageProperty())
        Thread(task).start()
    }

    private fun onCloseRequest() {
        result = ButtonType.CANCEL
        close()
    }
}


internal class Importer(
        private val connection: Connection?,
        private val categoryId: Int,
        private val psv: File,
        private val messagePrefix: String,
        private val dialog: Dialog<ButtonType>
) : Task<Long>() {
    private val sqlInsert = "INSERT INTO location (name, description, type, category_id) VALUES (?, ?, ?, ?)"

    override fun call() = import(calculateTotal())

    private fun calculateTotal() = psv.useLines { block -> block.count().toLong() }

    override fun done() {
        println("ITT VAGYOK")
        super.done()
        Platform.runLater {
            dialog.result = ButtonType.OK
            dialog.close()
        }
    }

    private fun import(total: Long): Long {
        var current = 1L
        connection?.let { connection ->
            connection.prepareStatement(sqlInsert).use { stmt ->
                psv.useLines { lines ->
                    lines.iterator().forEach {
                        val fields = it.split("|")
                        stmt.setString(1, fields[0])
                        stmt.setString(2, fields[1])
                        stmt.setString(3, fields[2])
                        stmt.setInt(4, categoryId)
                        stmt.executeUpdate()
                        updateProgress(current, total)
                        updateMessage("$messagePrefix ${current}/${total}...")
                        current++
                    }
                }
            }
        }
        return current
    }
}
