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
    private val categoryId: Int by param()
    private val controller = AddLocationController(locationManager)

    override fun onBeforeShow() {
        super.onBeforeShow()
        title = messages["DIALOG.TITLE_INSERT_LOCATION"]
        saved = false
        buttonSave.imageview(Image("images/save-mini.png"))
    }

    fun save() {
        controller.add(
                Location(0, textName.text, textDescription.text, checkBoxVisited.isSelected, textType.text, categoryId)
        )
        saved = true
        close()
    }

    fun isCanceled() = !saved
}


class AddLocationController(private val locationManager: LocationManager) : Controller() {
    internal fun add(location: Location) = locationManager.addLocation(location)
}
