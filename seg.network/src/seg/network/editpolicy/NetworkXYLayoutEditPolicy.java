/*
 * Created on 2005-01-30
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package seg.network.editpolicy;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;

import seg.network.edit.commands.CreateNodeCommand;
import seg.network.edit.commands.SetConstraintCommand;
import seg.network.model.network.EndPoint;
import seg.network.model.network.Network;
import seg.network.model.network.Node;
import seg.network.model.network.Responsibility;
import seg.network.model.network.StartPoint;

public class NetworkXYLayoutEditPolicy extends XYLayoutEditPolicy {

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createAddCommand(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	protected Command createAddCommand(EditPart child, Object constraint) {
		// TODO Auto-generated method stub
		return null;
	}



	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 */
	protected Command getCreateCommand(CreateRequest request) {
		Object	newObjectType = request.getNewObjectType();
		Command	createCommand = null;
		
		if( newObjectType == Node.class 
			|| newObjectType == Responsibility.class 
			|| newObjectType == StartPoint.class
			|| newObjectType == EndPoint.class) {
			CreateNodeCommand create = new CreateNodeCommand();
			create.setParent((Network)getHost().getModel());
			create.setLocation(request.getLocation());
			create.setNode( (Node)request.getNewObject() );
			create.setLabel("Create a node");
			createCommand = create;
		}

		return createCommand;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getDeleteDependantCommand(org.eclipse.gef.Request)
	 */
	protected Command getDeleteDependantCommand(Request request) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
	 */
	protected EditPolicy createChildEditPolicy(EditPart child) {
			return new NonResizableEditPolicy();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.XYLayoutEditPolicy#getMinimumSizeFor(org.eclipse.gef.GraphicalEditPart)
	 */
	protected Dimension getMinimumSizeFor(GraphicalEditPart child) {
		return child.getContentPane().getMinimumSize();
	}



	/* (non-Javadoc)
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
		SetConstraintCommand locationCommand = new SetConstraintCommand();
		locationCommand.setNode((Node)child.getModel());
		locationCommand.setNewPos(((Rectangle)constraint).getLocation());
		return locationCommand;
	}


}
