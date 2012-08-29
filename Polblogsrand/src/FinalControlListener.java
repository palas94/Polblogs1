import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.JPopupMenu;
import prefuse.controls.ControlAdapter;
import prefuse.controls.Control;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

public class FinalControlListener extends ControlAdapter implements Control
{
	public void itemClicked(VisualItem item, MouseEvent e)
	{
	      if(item instanceof NodeItem)
	      {
	    	  String nameofbook = ((String) item.get("label"));
	      String Orientation =  (String) item.get("value");
	      String sourcedata = (String) item.get("source");
	      JPopupMenu jpub = new JPopupMenu();
	      jpub.add("Label: " + nameofbook);
	      jpub.add("Value: " + Orientation);
	      jpub.add("Source: " + sourcedata);
	      
	      jpub.show(e.getComponent(),(int) item.getX(),
	                (int) item.getY());
	      }
	}
}