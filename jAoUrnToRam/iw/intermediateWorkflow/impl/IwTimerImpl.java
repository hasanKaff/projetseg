/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package intermediateWorkflow.impl;

import java.util.List;

import intermediateWorkflow.IntermediateWorkflowPackage;
import intermediateWorkflow.IwNodeConnection;
import intermediateWorkflow.IwTimer;
import iwToJavaInstantiator.NodeInstantiationStrategy;
import iwToJavaInstantiator.TimedSynchronizationNodeInstantiatorStrategy;
import iwToStepView.StepView;

import org.eclipse.emf.ecore.EClass;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Iw Timer</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public class IwTimerImpl extends IwWaitingPlaceImpl implements IwTimer {
	
	@Override
	public void appendEdges(StepView stepView) {
		List<IwNodeConnection> nodeConnections = getNodeConnectionFromCurrentStep(stepView);
		
		IwNodeConnection regularSucc = nodeConnections.get(0);
		regularSucc.appendTimerSucc(stepView);
		
		if(hasTimeoutPath()) {
			IwNodeConnection timeoutSucc = nodeConnections.get(1);
			timeoutSucc.appendTimeoutpathSucc(stepView);
		}
	}
	
	@Override
	public boolean hasTimeoutPath() {
		return this.succs.size() == 2;
	}
	
	@Override
	public String getImageName(){
		return "Timer16.gif";
	}
	
	@Override
	public  NodeInstantiationStrategy createStrategy() {
		return new TimedSynchronizationNodeInstantiatorStrategy(this, "TimedSynchronizationNode", getTransient());
	}
	
	/*@Override
	public String getInputProcessingNodeAction(){
		if(hasTimeoutPath()) {
			return "insert";
		}
		else {
			return super.getInputProcessingNodeAction();
		}
	}*/
	
	/*private NodeConnection timeoutSucc; 
	
	@Override
	public NodeConnection getTimeoutSucc() {
		return timeoutSucc;
	}

	@Override
	public void setTimeoutSucc(NodeConnection timeoutSucc) {
		this.timeoutSucc = timeoutSucc;
	}
	
	@Override
	public IwNodeConnection getIwTimeoutSucc(){
		return timeoutSucc.getIwNodeConnection();
	}*/
	
	/*private PathNode timeoutPathFirstNode;
	@Override
	public void setTimeoutpathFirstNode(PathNode node){
		timeoutPathFirstNode = node;
	}*/

	/*@Override
	public boolean hasTimeoutPath() {
		return timeoutSucc != null;
	}*/
	
	/*@Override
	public String getInputProcessingNodeAction() {
		if(hasTimeoutPath()){
			return "timeoutpath";
		}
		else {
			return super.getInputProcessingNodeAction();
		}
	}*/
	
	/*@Override
	public boolean hasTriggerPath() {
		return getPreds().size() == 2;
	}*/
	
	//private IwNode iwTimeoutPathFirstNode;
	/*private void exploreTimeoutPath(){
		iwTimeoutPathFirstNode = null;
		if(timeoutPathFirstNode != null) {
			IwStep step = createStep(false);
			iwTimeoutPathFirstNode = timeoutPathFirstNode.getIwEquivalentNode();
			iwTimeoutPathFirstNode.explore(step);
		}
	}*/
	
	/*@Override 
	protected IwNode getNextNodeToExplore(){
		IwNode nextNode = super.getNextNodeToExplore();
		
		if(isTimoutPathFirstNode(nextNode)){
			nextNode = getSucc(2).getTarget();
		}
		
		return nextNode;
	}*/
	
	/*@Override
	public void step_DeepFirstSearch(IwStep currentStep) {
		exploreTimeoutPath();
		
		super.step_DeepFirstSearch(currentStep);
	}*/
	
	/*private boolean isTimoutPathFirstNode(IwNode node){
		return iwTimeoutPathFirstNode == node;
	}*/
	
	
	/*@Override
	protected IwNodeConnection chooseSucc(){
		IwNodeConnection succ = super.chooseSucc();
		if(isTimeoutPathPred(succ)){
			succ = getSucc(2);
		}
		return succ;
	}*/
	
	/*private boolean isTimeoutPathPred(IwNodeConnection nodeConnection){
		IwNode target = nodeConnection.getTarget();
		
		boolean result = false;
		if(iwTimeoutPathFirstNode == target){
			result = true;
		}
		return result;
	}*/
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IwTimerImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return IntermediateWorkflowPackage.Literals.IW_TIMER;
	}

} //IwTimerImpl
