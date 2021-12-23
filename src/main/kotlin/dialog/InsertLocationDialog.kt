package hu.otilsoft.locations.dialog

import hu.otilsoft.locations.Location
import hu.otilsoft.locations.manager.LocationManager
import java.util.ResourceBundle
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.layout.VBox
import tornadofx.Controller
import tornadofx.View
import tornadofx.get
import tornadofx.imageview
import java.sql.Connection


class InsertLocationDialog : View() {
    init {
        messages = ResourceBundle.getBundle("insert-location-dialog")
    }

    override val root : VBox by fxml("/insert-location-dialog.fxml")
    private val buttonSave: Button by fxid("buttonSave")
    private val textName: TextField by fxid("textName")
    private val textDescription: TextField by fxid("textDescription")
    private val textType: TextField by fxid("textType")
    private val checkBoxVisited: CheckBox by fxid("checkBoxVisited")

    private var saved = false
    private val locationManager: LocationManager by param()
    private val modId: Int by param()
    private val controller: AddLocationController by inject()

    override fun onBeforeShow() {
        super.onBeforeShow()
        title = messages["DIALOG.TITLE_INSERT_LOCATION"]
        saved = false
        buttonSave.imageview(Image("images/save-mini.png"))
    }

    fun save() {
        controller.addLocation(
                locationManager, modId, textName.text, textDescription.text, textType.text, checkBoxVisited.isSelected
        )
        saved = true
        close()
    }

    fun isCanceled() = !saved
}

class AddLocationController : Controller() {
    fun addLocation(
            locationManager: LocationManager,
            modId: Int,
            name: String,
            description: String,
            type: String,
            visited: Boolean
    ) {
        locationManager.addLocation(Location(0, name, description, visited, type, modId))
    }
}
