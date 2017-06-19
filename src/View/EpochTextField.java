package View;

import Controller.Base.Main;
import javafx.scene.control.TextField;


/**
 * Created by ndutl on 19/06/2017.
 */
public class EpochTextField extends TextField {

    public EpochTextField() {
        super(Main.getEPOCHS());
    }
}
