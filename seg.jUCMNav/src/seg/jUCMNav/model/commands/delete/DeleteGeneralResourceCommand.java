package seg.jUCMNav.model.commands.delete;

import org.eclipse.gef.commands.CompoundCommand;

import seg.jUCMNav.model.commands.delete.internal.PreDeleteUrnModelElementCommand;
import seg.jUCMNav.model.commands.delete.internal.RemoveURNmodelElementCommand;
import ucm.performance.GeneralResource;

/**
 * Command to delete a resource.
 * 
 * @author jkealey
 *  
 */
public class DeleteGeneralResourceCommand extends CompoundCommand {

    public DeleteGeneralResourceCommand(GeneralResource resx) {
        
        setLabel("Delete Resource");

        add(new PreDeleteUrnModelElementCommand(resx));
        add(new RemoveURNmodelElementCommand(resx));
        
        
    }
}