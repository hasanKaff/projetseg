/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package ucm.map.impl;

import java.util.List;

import intermediateWorkflow.IntermediateWorkflowFactory;
import intermediateWorkflow.IwNode;
import intermediateWorkflow.IwNodeConnection;
import intermediateWorkflow.IwWaitingPlace;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import ucm.map.MapPackage;
import ucm.map.NodeConnection;
import ucm.map.PathNode;
import ucm.map.WaitKind;
import ucm.map.WaitingPlace;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Waiting Place</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link ucm.map.impl.WaitingPlaceImpl#getWaitType <em>Wait Type</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class WaitingPlaceImpl extends PathNodeImpl implements WaitingPlace {
	
	protected IwWaitingPlace iwWaitingPlace;
	@Override
	public void buildIwNodeTemplate() {
		iwWaitingPlace = IntermediateWorkflowFactory.eINSTANCE.createIwWaitingPlace();
		iwWaitingPlace.setName(nameOrPrefixId("WaitingPlace"));
		iwWaitingPlace.setTransient(getWaitType().equals(WaitKind.TRANSIENT));
		addIwEquivalentNodeAfterOutIn(iwWaitingPlace);
	}

	@Override
	public IwWaitingPlace getIwWaitingPlace() {
		return iwWaitingPlace;
	}
	
	@Override
	public void linkTriggerPath(NodeConnection pred, NodeConnection succ) {
		IwNodeConnection iwPred = pred.getIwNodeConnection();
		//IwNodeConnection iwSucc = succ.getIwNodeConnection();
		
		PathNode target = (PathNode)succ.getTarget();
		IwNode iwTarget = target.getIwEquivalentNode();
		
		iwWaitingPlace.linkTriggerPath(iwPred, iwTarget);
	}
	
	/*@Override
	public void invokeBuildOnNodeConnections() {
		//for(NodeConnection nodeConnection: succAsNodeConnection()){
			//nodeConnection.build();
		//}
		if(hasTriggerPathAtEmptyPoint()) {
			NodeConnection waitingPathSucc = getWaitingPathSucc();
			waitingPathSucc.build();
		
			NodeConnection triggerPathSucc = getTriggerPathSucc();
			triggerPathSucc.build("trigger");
		} else {
			super.invokeBuildOnNodeConnections();
		}
	}*/
	
	/*public boolean hasTriggerPathAtEmptyPoint(){
		return false;
	}
	
	
	public NodeConnection getWaitingPathSucc() {
		List<NodeConnection> nodeConnection = succAsNodeConnection();
		NodeConnection succ = nodeConnection.get(0);
		return succ;
	}
	
	public NodeConnection getTriggerPathSucc() {
		List<NodeConnection> nodeConnection = succAsNodeConnection();
		NodeConnection succ = nodeConnection.get(1);
		return succ;
	}*/
	
	
	/**
	 * The default value of the '{@link #getWaitType() <em>Wait Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWaitType()
	 * @generated
	 * @ordered
	 */
	protected static final WaitKind WAIT_TYPE_EDEFAULT = WaitKind.TRANSIENT;

	/**
	 * The cached value of the '{@link #getWaitType() <em>Wait Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWaitType()
	 * @generated
	 * @ordered
	 */
	protected WaitKind waitType = WAIT_TYPE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected WaitingPlaceImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MapPackage.Literals.WAITING_PLACE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WaitKind getWaitType() {
		return waitType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWaitType(WaitKind newWaitType) {
		WaitKind oldWaitType = waitType;
		waitType = newWaitType == null ? WAIT_TYPE_EDEFAULT : newWaitType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MapPackage.WAITING_PLACE__WAIT_TYPE, oldWaitType, waitType));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MapPackage.WAITING_PLACE__WAIT_TYPE:
				return getWaitType();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case MapPackage.WAITING_PLACE__WAIT_TYPE:
				setWaitType((WaitKind)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case MapPackage.WAITING_PLACE__WAIT_TYPE:
				setWaitType(WAIT_TYPE_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case MapPackage.WAITING_PLACE__WAIT_TYPE:
				return waitType != WAIT_TYPE_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (waitType: ");
		result.append(waitType);
		result.append(')');
		return result.toString();
	}

} //WaitingPlaceImpl