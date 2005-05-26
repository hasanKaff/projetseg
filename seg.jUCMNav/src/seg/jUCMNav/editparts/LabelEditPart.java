/*
 * Created on Mar 27, 2005
 */
package seg.jUCMNav.editparts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;

import seg.jUCMNav.editpolicies.directEdit.ExtendedDirectEditManager;
import seg.jUCMNav.editpolicies.directEdit.LabelCellEditorLocator;
import seg.jUCMNav.editpolicies.directEdit.LabelDirectEditPolicy;
import seg.jUCMNav.editpolicies.element.LabelComponentEditPolicy;
import seg.jUCMNav.figures.EditableLabel;
import seg.jUCMNav.figures.LabelFigure;
import seg.jUCMNav.figures.util.JUCMNavFigure;
import ucm.map.ComponentRef;
import ucm.map.MapPackage;
import ucm.map.PathNode;
import ucm.map.RespRef;
import urncore.ComponentElement;
import urncore.Label;
import urncore.Responsibility;
import urncore.UCMmodelElement;

/**
 * Based on Etienne's implementation of PathNodeEditPart
 * 
 * @author Jordan McManus
 */
public class LabelEditPart extends ModelElementEditPart {
    private UCMmodelElement modelElement;
    private ComponentElement comp = null;
    private Responsibility resp = null;

    private static final int LABEL_PADDING_X = 6;
    private static final int LABEL_PADDING_Y = 4;

    protected DirectEditManager manager;

    public LabelEditPart(Label model, UCMmodelElement modelElement) {
        super();
        setModel(model);
        this.modelElement = modelElement;
    }

    public LabelEditPart(Label model) {
        super();
        setModel(model);
        modelElement = (UCMmodelElement) model.eContainer();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.EditPart#activate()
     */
    public void activate() {
        if (!isActive()) {
            modelElement.eAdapters().add(this);
            if (modelElement instanceof ComponentRef) {
                comp = ((ComponentRef) modelElement).getCompDef();
                if (comp != null)
                    comp.eAdapters().add(this);
            } else if (modelElement instanceof RespRef) {
                resp = ((RespRef) modelElement).getRespDef();
                if (resp != null)
                    resp.eAdapters().add(this);
            }
        }
        super.activate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.EditPart#deactivate()
     */
    public void deactivate() {
        if (isActive()) {
            modelElement.eAdapters().remove(this);
            if (modelElement instanceof ComponentRef) {
                if (comp != null)
                    comp.eAdapters().remove(this);
                comp = null;
            } else if (modelElement instanceof RespRef) {
                if (resp != null)
                    resp.eAdapters().remove(this);
                resp = null;
            }
        }
        super.deactivate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.editparts.ModelEditPart#getModelObj()
     */
    public Label getModelObj() {
        return (Label) getModel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see seg.jUCMNav.editparts.ModelEditPart#getModelObj()
     */
    public UCMmodelElement getUCMmodelElement() {
        return modelElement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    protected IFigure createFigure() {
        EditableLabel label = new EditableLabel("");
        return new LabelFigure(label);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new LabelComponentEditPolicy());
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new LabelDirectEditPolicy());
    }

    public LabelFigure getLabelFigure() {
        return (LabelFigure) getFigure();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    protected void refreshVisuals() {
        if (modelElement != null) {
            LabelFigure labelFigure = getLabelFigure();
            EditableLabel label = labelFigure.getLabel();

            if (modelElement instanceof ComponentRef) {
                ComponentElement componentElement = ((ComponentRef) modelElement).getCompDef();
                if (componentElement != null)
                    label.setText(componentElement.getName());
            } else if (modelElement instanceof RespRef) {
                Responsibility responsibility = ((RespRef) modelElement).getRespDef();
                if (responsibility != null)
                    label.setText(responsibility.getName());
            } else {
                label.setText(modelElement.getName());
            }

            Dimension dimEditableLabel = labelFigure.getLabel().getPreferredSize().getCopy();
            Dimension newLabelDimension = new Dimension(dimEditableLabel.width + LABEL_PADDING_X, dimEditableLabel.height + LABEL_PADDING_Y);

            //The position of the new figure
            Point location = calculateModelElementPosition(getModelObj(), newLabelDimension);

            Rectangle bounds = new Rectangle(location, newLabelDimension);
            figure.setBounds(bounds);
            label.setBounds(bounds);
            // notify parent container of changed position & location
            // if this line is removed, the XYLayoutManager used by the parent container
            // (the Figure of the ShapesDiagramEditPart), will not know the bounds of this figure
            // and will not draw it correctly.
            if (getParent() != null) {
                ((GraphicalEditPart) getParent()).setLayoutConstraint(this, figure, bounds);
            }

        }
    }

    private Point calculateModelElementPosition(Label label, Dimension labelDimension) {
        Point location;

        if (modelElement instanceof PathNode) {
            PathNode node = (PathNode) modelElement;
            location = new Point(node.getX() - labelDimension.width / 2 - label.getDeltaX(), node.getY()
                    - (labelDimension.height + JUCMNavFigure.getDimension(modelElement).height / 2) - label.getDeltaY());
        } else if (modelElement instanceof ComponentRef) {
            ComponentRef component = (ComponentRef) modelElement;
            location = new Point(component.getX() - label.getDeltaX(), component.getY() - label.getDeltaY() - labelDimension.height);
        } else {
            location = new Point(0, 0);
        }

        return location;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.emf.common.notify.Adapter#notifyChanged(org.eclipse.emf.common.notify.Notification)
     */
    public void notifyChanged(Notification notification) {
        int type = notification.getEventType();
        int featureId = notification.getFeatureID(MapPackage.class);

        if (type == Notification.SET) {
            if (featureId == MapPackage.COMPONENT_REF__COMP_DEF) {
                if (modelElement instanceof ComponentRef) {
                    if (comp != null)
                        comp.eAdapters().remove(this);
                    comp = ((ComponentRef) modelElement).getCompDef();
                    if (comp != null)
                        comp.eAdapters().add(this);
                }
            } else if (featureId == MapPackage.RESP_REF__RESP_DEF)
                if (modelElement instanceof RespRef) {
                    if (resp != null)
                        resp.eAdapters().remove(this);
                    resp = ((RespRef) modelElement).getRespDef();
                    if (resp != null)
                        resp.eAdapters().add(this);
                }
        }

        if (getParent() != null) {
            ((MapAndPathGraphEditPart) getParent()).notifyChanged(notification);
            refreshVisuals();
        }
    }

    /**
     * Show direct edit on label on double click, f2 or delay.
     */
    public void performRequest(Request request) {
        if (request.getType() == RequestConstants.REQ_DIRECT_EDIT || request.getType() == RequestConstants.REQ_OPEN) {
            if (request instanceof DirectEditRequest && !directEditHitTest(((DirectEditRequest) request).getLocation().getCopy()))
                return;
            performDirectEdit();
        }
    }

    /**
     * For direct edit, verify location.
     * 
     * @param requestLoc
     * @return
     */
    private boolean directEditHitTest(Point requestLoc) {
        LabelFigure figure = (LabelFigure) getFigure();
        EditableLabel nameLabel = figure.getLabel();
        nameLabel.translateToRelative(requestLoc);
        if (nameLabel.containsPoint(requestLoc))
            return true;
        return false;
    }

    /**
     * Opens the direct edit manager.
     *  
     */
    protected void performDirectEdit() {
        if (manager == null) {
            LabelFigure figure = (LabelFigure) getFigure();
            EditableLabel nameLabel = figure.getLabel();

            ICellEditorValidator validator = new ICellEditorValidator() {
                public String isValid(Object value) {
                    return "";
                }
            };

            manager = new ExtendedDirectEditManager(this, TextCellEditor.class, new LabelCellEditorLocator(nameLabel), nameLabel, validator);
        }
        manager.show();
    }

    /**
     * @param handles
     *            the name change during an edit
     */
    public void handleNameChange(String value) {
        LabelFigure tableFigure = (LabelFigure) getFigure();
        EditableLabel label = tableFigure.getLabel();
        label.setVisible(false);
        refreshVisuals();
    }

    /**
     * Reverts to existing name in model when exiting from a direct edit (possibly before a commit which will result in a change in the label value)
     */
    public void revertNameChange() {
        LabelFigure tableFigure = (LabelFigure) getFigure();
        EditableLabel label = tableFigure.getLabel();
        label.setVisible(true);
        refreshVisuals();
    }
}