package seg.jUCMNav.editparts;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;

import seg.jUCMNav.editors.UCMNavMultiPageEditor;

/**
 * Created 2005-03-24
 * 
 * This class only change the order of the layers of the root edit part.
 * 
 * @author Etienne Tremblay
 */
public class ConnectionOnBottomRootEditPart extends ScalableFreeformRootEditPart {

	public static final String COMPONENT_LAYER = "COMPONENT";
	
	private UCMNavMultiPageEditor multiPageEditor;

	/**
	 *  
	 */
	public ConnectionOnBottomRootEditPart(UCMNavMultiPageEditor editor) {
		super();
		multiPageEditor = editor;
	}

	/**
	 * Overwrite this function and add the connection layer before the primary layer. This will make the nodes display on top of the connections.
	 */
	protected LayeredPane createPrintableLayers() {
		FreeformLayeredPane layeredPane = new FreeformLayeredPane();

		FreeformLayer comp = new FreeformLayer();
		comp.setLayoutManager(new FreeformLayout());

		layeredPane.add(comp, COMPONENT_LAYER);
		layeredPane.add(new ConnectionLayer(), CONNECTION_LAYER);
		layeredPane.add(new FreeformLayer(), PRIMARY_LAYER);
		return layeredPane;
	}

	/**
	 * @return Returns the MultiPageEditor.
	 */
	public UCMNavMultiPageEditor getMultiPageEditor() {
		return multiPageEditor;
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.gef.editparts.ScalableFreeformRootEditPart#getScaledLayers()
     */
    public LayeredPane getScaledLayers() {
        return super.getScaledLayers();
    }
}