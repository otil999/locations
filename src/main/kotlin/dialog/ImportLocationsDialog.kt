package hu.otilsoft.locations.dialog

import hu.otilsoft.locations.Category
import hu.otilsoft.locations.manager.LocationManager
import javafx.application.Platform
import javafx.concurrent.Task
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.layout.VBox
import javafx.stage.Stage
import tornadofx.*
import java.io.File
import java.sql.Connection
import java.util.*


class ImportLocationsDialog : View() {
    init {
        messages = ResourceBundle.getBundle("import-locations-dialog")
        title = messages["DIALOG.TITLE"]
    }

    override val root: VBox by fxml("/import-locations-dialog.fxml")
    private val progressImport: ProgressBar by fxid("progressImport")
    private val labelImport: Label by fxid("labelImport")

    private val locationManager: LocationManager by param()
    private val psv: File by param()
    private val category: Category by param()

    override fun onBeforeShow() {
        super.onBeforeShow()
        val task = Importer(locationManager.connection, category.id, psv, messages["LABEL.IMPORT"], currentStage!!)
        progressImport.progressProperty().bind(task.progressProperty())
        labelImport.textProperty().bind(task.messageProperty())
        Thread(task).start()
    }
}


internal class Importer(
        private val connection: Connection?,
        private val categoryId: Int,
        private val psv: File,
        private val messagePrefix: String,
        private val stage: Stage
) : Task<Long>() {
    private val sqlInsert = "INSERT INTO location (name, description, type, category_id) VALUES (?, ?, ?, ?)"

    override fun call() = import(calculateTotal())

    private fun calculateTotal() = psv.useLines { block -> block.count().toLong() }

    override fun done() {
        super.done()
        Platform.runLater {
            stage.close()
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
