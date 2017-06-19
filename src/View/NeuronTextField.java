package View;

import Controller.Base.Main;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Created by ndutl on 19/06/2017.
 */
public class NeuronTextField extends TextField {

    public NeuronTextField() {
        super(Main.getNEURONS());
    }
}
