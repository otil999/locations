package hu.otilsoft.locations.dialog

import hu.otilsoft.locations.NoButtonBarDialogPane
import hu.otilsoft.locations.imageView
import hu.otilsoft.locations.manager.ProjectManager
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.stage.Modality
import javafx.stage.Window
import java.util.*


class EditProjectDialog(
    owner: Window,
    private val update: Boolean,
    private val categoryName: String,
    projectManager: ProjectManager) : Dialog<ButtonType>()
{
    init {
        val resources = ResourceBundle.getBundle("edit-project-dialog")
        val fxmlLoader = FXMLLoader(this::class.java.getResource("/edit-project-dialog.fxml"), resources)
        fxmlLoader.setController(this)

        initOwner(owner)
        initModality(Modality.APPLICATION_MODAL)

        dialogPane = NoButtonBarDialogPane()
        dialogPane.content = fxmlLoader.load()
        dialogPane.scene.window.setOnCloseRequest { _ -> run { onCloseRequest() } }

        title = resources.getString(if (update) { "TITLE.RENAME_PROJECT" } else { "TITLE.NEW_PROJECT" })
        textName.text = categoryName
        buttonSave.graphic = imageView(resources, "BUTTON.SAVE.ICON")
    }

    private val controller = EditProjectController(projectManager)

    @FXML private lateinit var textName: TextField
    @FXML private lateinit var buttonSave: Button

    private fun onCloseRequest() {
        result = ButtonType.CANCEL
        close()
    }

    @FXML
    private fun onSave() {
        if (update) {
            controller.rename(categoryName, textName.text)
        } else {
            controller.create(textName.text)
        }
        result = ButtonType.OK
        close()
    }

    fun savedCategory(): String = textName.text
}


internal class EditProjectController(private val projectManager: ProjectManager) {
    fun rename(oldName: String, newName: String) = projectManager.renameDatabase(oldName, newName)
    fun create(name: String) = projectManager.createDatabase(name)
}
