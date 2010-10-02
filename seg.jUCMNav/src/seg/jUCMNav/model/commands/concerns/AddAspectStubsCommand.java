package seg.jUCMNav.model.commands.concerns;

import java.util.List;

import org.eclipse.gef.commands.CompoundCommand;

import seg.jUCMNav.Messages;
import seg.jUCMNav.aourn.composer.AspectMarkerMappings;
import seg.jUCMNav.model.ModelCreationFactory;
import seg.jUCMNav.model.commands.create.AddInBindingCommand;
import seg.jUCMNav.model.commands.create.AddOutBindingCommand;
import seg.jUCMNav.model.commands.create.AddPluginCommand;
import seg.jUCMNav.model.commands.transformations.SplitLinkCommand;
import ucm.map.EndPoint;
import ucm.map.NodeConnection;
import ucm.map.PathNode;
import ucm.map.StartPoint;
import ucm.map.Stub;
import ucm.map.UCMmap;
import urn.URNspec;

/**
 * CompoundCommand to add aspect stubs, first adds an aspect marker, then its plug-in map, and then its in/out-bindings
 *
 * @author gunterm
 */
public class AddAspectStubsCommand extends CompoundCommand {

    /**
     * @param urn
     *            URNspec to which to add the aspect markers
     * @param composeResult
     *            final result from the matching/composition algorithm
     *
     */
    public AddAspectStubsCommand(URNspec urn, List<AspectMarkerMappings> composeResult) {
        setLabel(Messages.getString("ApplyConcernCommand.ApplyConcern")); //$NON-NLS-1$
        for (int i = 0; i < composeResult.size(); i++) {
        	AspectMarkerMappings amm = composeResult.get(i);
        	// add the aspect stub
        	UCMmap baseMap = amm.getBaseMap();
        	int type = amm.getType();
        	PathNode pn = (PathNode) ModelCreationFactory.getNewObject(urn, Stub.class, type);
        	pn.setName(amm.getCounter().toString());
        	NodeConnection nc = amm.getInsertionPoint();
        	int x = composeResult.get(i).getInsertionXCoordinate();
        	int y = composeResult.get(i).getInsertionYCoordinate();
            add(new SplitLinkCommand(baseMap, pn, nc, x, y));
            // add plug-in to aspect stub
            UCMmap aspectMap = amm.getAspectMap();
            add(new AddPluginCommand((Stub) pn, aspectMap));
            // add in-bindings for aspect stub
            Object o = amm.getInBindingPluginElement();
        	// only if the second mapping has been defined for this aspect marker
            if (o != null) {
               	add(new AddInBindingCommand((Stub) pn, o));
               	// TODO visualization for now
               	if (o instanceof NodeConnection) {
               		pn.setDescription("in to pointcut stub " + ((PathNode) ((NodeConnection) o).getSource()).getName());
               	} else {
               		pn.setDescription("in to " + ((StartPoint) o).getName() + "[" + ((StartPoint) o).getId() + "]"); 
               	}            	
            }
            // add out-bindings for aspect stub
            Object o2 = amm.getOutBindingPluginElement();
        	// only if the second mapping has been defined for this aspect marker
            if (o2 != null) {
               	add(new AddOutBindingCommand((Stub) pn, o2));
               	// TODO visualization for now
               	String desc = pn.getDescription();
               	if (o2 instanceof NodeConnection) {
               		pn.setDescription(desc + "   -----   out from pointcut stub " + ((PathNode) ((NodeConnection) o2).getTarget()).getName());
               	}
               	else {
               		pn.setDescription(desc + "   -----   out from " + ((EndPoint) o2).getName() + "[" + ((EndPoint) o2).getId() + "]");
               	}            	
            }
        }
    }

}