package seg.jUCMNav.strategies;

import grl.Actor;
import grl.ElementLink;
import grl.Evaluation;
import grl.EvaluationStrategy;
import grl.GRLspec;
import grl.ImportanceType;
import grl.IntentionalElement;
import grl.IntentionalElementType;
import grl.QualitativeLabel;
import grl.kpimodel.KPIEvalValueSet;
import grl.kpimodel.KPIInformationConfig;
import grl.kpimodel.KPIInformationElement;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.ui.PlatformUI;

import seg.jUCMNav.JUCMNavPlugin;
import seg.jUCMNav.Messages;
import seg.jUCMNav.editors.UCMNavMultiPageEditor;
import seg.jUCMNav.editors.UrnEditor;
import seg.jUCMNav.editparts.URNRootEditPart;
import seg.jUCMNav.editparts.kpiTreeEditparts.KPIRootEditPart;
import seg.jUCMNav.editparts.kpiViewEditparts.AbstractKPIViewEditPart;
import seg.jUCMNav.extensionpoints.IGRLStrategyAlgorithm;
import seg.jUCMNav.model.ModelCreationFactory;
import seg.jUCMNav.model.commands.create.AddEvaluationCommand;
import seg.jUCMNav.model.commands.create.AddKPIInformationConfigCommand;
import seg.jUCMNav.model.commands.delete.DeleteEvaluationCommand;
import seg.jUCMNav.model.util.MetadataHelper;
import seg.jUCMNav.views.preferences.StrategyEvaluationPreferences;
import urn.URNspec;
import urncore.Metadata;

/**
 * This class is a singleton responsible to manage the current strategy. It does the evaluation calculation for IntentionalElement, create the Evaluation and
 * return the value of the evaluation given an IntentionalElement for the current strategy.
 * 
 * @author Jean-Fran�ois Roy, pchen, sghanava
 * 
 */
public class EvaluationStrategyManager {

    /**
     * Metadata name used to store run-time GRL numerical evaluations
     */
    public static final String METADATA_NUMEVAL = "_numEval"; //$NON-NLS-1$
    /**
     * Metadata name used to store run-time GRL qualitative evaluations
     */
    public static final String METADATA_QUALEVAL = "_qualEval"; //$NON-NLS-1$


    private static HashMap<UCMNavMultiPageEditor, EvaluationStrategyManager> strategyManagerInstances = null;
 // just in case we're actually accessing it via some non UI thread during export and/or before the app loads. 
    private static EvaluationStrategyManager noEditorStrategyManagerInstance = null;  

    private boolean canRefresh;
    private boolean differenceMode = false;
    
    private UCMNavMultiPageEditor multieditor;
    private ScrollingGraphicalViewer kpiViewer;
    private TreeViewer kpiListViewer;
    private HashMap evaluations = new HashMap(); // HashMap to keep link between intentionalElement and the evaluation for a particular strategy
    private HashMap comparisonEvaluations = new HashMap(); // HashMap to keep link between intentionalElement and the evaluation for the second strategy in difference mode
    private EvaluationStrategy strategy, strategy1 = null, strategy2 = null; // strategy1, strategy2 used in difference mode
    private IGRLStrategyAlgorithm algo;
    private HashMap kpiInformationConfigs = new HashMap();
    
    public static synchronized EvaluationStrategyManager getInstance(UCMNavMultiPageEditor multieditor, boolean canRefresh) {
    	
    	if (strategyManagerInstances == null)
    	{
    		strategyManagerInstances = new HashMap<UCMNavMultiPageEditor, EvaluationStrategyManager>();
    	}
    	
    	EvaluationStrategyManager soleInstance = null;
    	
		if (multieditor == null) {
			if (PlatformUI.getWorkbench() != null
					&& PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
					&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null
					&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof UCMNavMultiPageEditor) {
				multieditor = (UCMNavMultiPageEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			}
		}

		if (multieditor != null) {
			if (strategyManagerInstances.get(multieditor) == null)
				strategyManagerInstances.put(multieditor,
						new EvaluationStrategyManager());
			soleInstance = strategyManagerInstances.get(multieditor);
		} else {
			// fallback for non-UI threads (not even sure if this exists, but
			// better be safe than sorry
		    if (noEditorStrategyManagerInstance == null) noEditorStrategyManagerInstance = new EvaluationStrategyManager();
			soleInstance = noEditorStrategyManagerInstance;
		}

		soleInstance.canRefresh = canRefresh;
		return soleInstance;
    }

    public static synchronized EvaluationStrategyManager getInstance(UCMNavMultiPageEditor multieditor) {
    	return getInstance(multieditor, true);
    }
    
    public static synchronized EvaluationStrategyManager getInstance(boolean canRefresh) {
        return getInstance(null, canRefresh);
    }
    public static synchronized EvaluationStrategyManager getInstance() {
    	return getInstance(null, true);
    }
    
    public static void releaseEnvironment(UCMNavMultiPageEditor editor)
    {
    	if (editor!=null && strategyManagerInstances.containsKey(editor)) {
    		strategyManagerInstances.remove(editor);
    	}
    }


    /**
     * 
     */
    private EvaluationStrategyManager() {

    }

    public synchronized void calculateEvaluation() {

    	if( differenceMode && strategy1 != null && strategy2 != null && strategy2 != strategy1 ) { // calculate two evaluations and compute the difference
    		
        	if( JUCMNavPlugin.isInDebug() ){
        		System.out.println( "calculateEvaluation() diff mode strategy1: " + strategy1.getName() + " strategy2: " + strategy2.getName() ); //$NON-NLS-1$
        	}
    		
        	EvaluationStrategy currentStrategy = strategy;
        	
    		canRefresh = false; // turn off refresh until difference is computed
    		strategy = strategy1;
    		
        	if( JUCMNavPlugin.isInDebug() ){
        		System.out.println( "calculateEvaluation() diff mode first call "  ); //$NON-NLS-1$
        	}

    		calculateSingleEvaluation();
    		
        	if( JUCMNavPlugin.isInDebug() ){
        		System.out.println( "calculateEvaluation() diff mode  first evaluations.keySet().size() = " +  evaluations.keySet().size() ); //$NON-NLS-1$
        	}
    		
    		HashMap firstEvaluation = evaluations; // keep reference to original evaluations map
    		evaluations = comparisonEvaluations; // use second HashMap for second strategy
    		
        	if( JUCMNavPlugin.isInDebug() ){
        		System.out.println( "calculateEvaluation() diff mode second call "  ); //$NON-NLS-1$
        	}

        	strategy = strategy2;
    		calculateSingleEvaluation();
    		
        	if( JUCMNavPlugin.isInDebug() ){
        		System.out.println( "calculateEvaluation() diff mode second evaluations.keySet().size() = " +  evaluations.keySet().size() ); //$NON-NLS-1$
        	}

        	for( Iterator iter = evaluations.keySet().iterator(); iter.hasNext(); ) {
    			
    			IntentionalElement ie = (IntentionalElement) iter.next();
    			Evaluation firstEval = (Evaluation) comparisonEvaluations.get( ie );
    			Evaluation secondEval = (Evaluation) evaluations.get( ie );
    			
    			int diffValue = secondEval.getEvaluation() - firstEval.getEvaluation();
    			
            	if( JUCMNavPlugin.isInDebug() ){
            		System.out.println( "calculateEvaluation() diff mode IntentionalElement: " + ie.getName() + " 2 eval: " +  secondEval.getEvaluation() + " 1 eval: " +  firstEval.getEvaluation()
            				+ " diff: " + diffValue ); //$NON-NLS-1$
            	}
    			
    			
    			secondEval.setEvaluation( diffValue );
    			syncIntentionalElementQualitativeEvaluation(secondEval, diffValue);
    		}
    		
        	strategy = currentStrategy; // reset strategy
    		canRefresh = true;
    		refreshDiagrams();
    		
    	} else {
    		calculateSingleEvaluation();
    	}
    }
    
    public synchronized void calculateSingleEvaluation() {
        if (strategy == null) {
            return;
        }
        setupEvaluationAlgorithm();
        
    	if( differenceMode && JUCMNavPlugin.isInDebug() ){
    		System.out.println( "calculateSingleEvaluation() diff mode"  ); //$NON-NLS-1$
    	}
        
        
        long before = System.currentTimeMillis();
        algo.init(strategy, evaluations);
        if(algo.isConstraintSolverAlgorithm()) {
        	processConstraintSolverAlgorithm();
        } else {
        	processNonConstraintSolverAlgorithm();
        }
        long after = System.currentTimeMillis();
        System.out.println("Time spent: " + (after - before) + " milliseconds"); //$NON-NLS-1$ //$NON-NLS-2$
        
        System.out.flush();
        
        refreshDiagrams();
    }

    private synchronized void refreshDiagrams() {
        // Refresh all the diagrams if canRefresh set to true
        if (canRefresh && multieditor != null) {
            for (int i = 0; i < multieditor.getPageCount(); i++) {
                UrnEditor u = (UrnEditor) multieditor.getEditor(i);
                ((URNRootEditPart) u.getGraphicalViewer().getRootEditPart()).refreshChildren();
            }
        }

        // Refresh the kpi view if canRefresh set to true
        if (canRefresh && kpiViewer != null) {
            EditPart ep = kpiViewer.getContents();
            if (ep instanceof AbstractKPIViewEditPart) {
                ((AbstractKPIViewEditPart) ep).refreshChildren();
            }
        }

        // Refresh the kpi list view if canRefresh set to true
        if (canRefresh && kpiListViewer != null) {
            EditPart ep = kpiListViewer.getContents();
            if (ep instanceof KPIRootEditPart) {
                ((KPIRootEditPart) ep).refreshIndicatorTreeEditPart(ep);
            }
        }
    }
    
	private void processNonConstraintSolverAlgorithm() {
		while (algo.hasNextNode()) {
			IntentionalElement element = algo.nextNode();
			String elementName = element.getName();
			Evaluation eval = (Evaluation) evaluations.get(element);
			int val = algo.getEvaluation(element);
			eval.setEvaluation(val);
			syncIntentionalElementQualitativeEvaluation(eval, val);
			setEvaluationMetadata(element, eval);
		}
	}

	private void processConstraintSolverAlgorithm() {
		Hao2011Algorithm hao2011Algorithm = (Hao2011Algorithm) algo;
		hao2011Algorithm.calculate();
		while (hao2011Algorithm.hasNextNode()) {
			IntentionalElement element = hao2011Algorithm.nextNode();
			Evaluation eval = (Evaluation) evaluations.get(element);
			int val = eval.getEvaluation();
			syncIntentionalElementQualitativeEvaluation(eval, val);
			//setEvaluationMetadata(element, eval);
			setEvaluationMetadata(element, val, eval.getQualitativeEvaluation().toString());
		}
	}

    private void setEvaluationMetadata(IntentionalElement element, int val, String qualEvalAsString) {
        String numEvalAsString = Integer.toString(val);
        Metadata metaNumerical = MetadataHelper.getMetaDataObj(element, METADATA_NUMEVAL);
        if (metaNumerical != null) {
            // Run-time metadata already exist for this element
            metaNumerical.setValue(numEvalAsString);
        } else {
            // Add new run-time metadata for this element
            URNspec urnSpec = element.getGrlspec().getUrnspec();
            MetadataHelper.addMetaData(urnSpec, element, METADATA_NUMEVAL, numEvalAsString);
        }

        Metadata metaQuantitative = MetadataHelper.getMetaDataObj(element, METADATA_QUALEVAL);
        if (metaQuantitative != null) {
            // Run-time metadata already exist for this element
            metaQuantitative.setValue(qualEvalAsString);
        } else {
            // Add new run-time metadata for this element
            URNspec urnSpec = element.getGrlspec().getUrnspec();
            MetadataHelper.addMetaData(urnSpec, element, METADATA_QUALEVAL, qualEvalAsString);
        }
    }

	public synchronized int getActorEvaluation(Actor actor) {
        int actorEval = algo.getActorEvaluation(actor);
        setEvaluationMetadata(actor, actorEval);
        return actorEval;
    }

    public synchronized int getEvaluation(IntentionalElement elem) {
        Evaluation temp = (Evaluation) evaluations.get(elem);
        if (temp == null && strategy != null && strategy.getGrlspec() != null && strategy.getGrlspec().getUrnspec() != null) {
            temp = (Evaluation) ModelCreationFactory.getNewObject(strategy.getGrlspec().getUrnspec(), Evaluation.class);
            evaluations.put(elem, temp);
            return temp.getEvaluation();
        } else if (temp != null) {
            return temp.getEvaluation();
        } else
            return Evaluation.EVALUATION_UNDEFINED;

    }

    /**
     * This method returns the evaluation strategy algorithm if it has been prepared, if not it will set it up
     * 
     * @return the evaluation strategy algorithm
     * 
     */
    public synchronized IGRLStrategyAlgorithm getEvaluationAlgorithm() {
        if (algo == null)
            setupEvaluationAlgorithm();
        return algo;
    }

    /**
     * Prepares the value from the properties preference page and creates the algorithm accordingly
     * 
     */
    public synchronized void setupEvaluationAlgorithm() {
        String algoChoice = StrategyEvaluationPreferences.getAlgorithm();

        if ((StrategyEvaluationPreferences.MIXED_ALGORITHM + "").equals(algoChoice)) //$NON-NLS-1$
            algo = new MixedGRLStrategyAlgorithm();
        else if ((StrategyEvaluationPreferences.QUANTITATIVE_ALGORITHM + "").equals(algoChoice)) //$NON-NLS-1$
            algo = new QuantitativeGRLStrategyAlgorithm();
        else if ((StrategyEvaluationPreferences.QUALITATIVE_ALGORITHM + "").equals(algoChoice)) //$NON-NLS-1$
            algo = new QualitativeGRLStrategyAlgorithm();
        else if ((StrategyEvaluationPreferences.FORMULA_BASED_ALGORITHM + "").equals(algoChoice)) //$NON-NLS-1$
            algo = new FormulaBasedGRLStrategyAlgorithm();
        else if ((StrategyEvaluationPreferences.CONDITIONAL_GRL_ALGORITHM + "").equals(algoChoice)) //$NON-NLS-1$
            algo = new ConditionalBasedGRLStrategyAlgorithm();
        else if((StrategyEvaluationPreferences.CONSTRAINT_SOLVER_ALGORITHM + "").equals(algoChoice)) //$NON-NLS-1$
        	algo = new Hao2011Algorithm();
        else
            algo = new DefaultGRLStrategyAlgorithm();

    }

    public synchronized String getLevelOfDimension(KPIInformationElement elem) {
        KPIInformationConfig temp = (KPIInformationConfig) kpiInformationConfigs.get(elem);
        if (temp == null && strategy != null && strategy.getGrlspec() != null && strategy.getGrlspec().getUrnspec() != null) {
            temp = (KPIInformationConfig) ModelCreationFactory.getNewObject(strategy.getGrlspec().getUrnspec(), KPIInformationConfig.class);
            kpiInformationConfigs.put(elem, temp);
            return temp.getLevelOfDimension();
        } else if (temp != null) {
            return temp.getLevelOfDimension();
        } else
            return ""; //$NON-NLS-1$

    }

    public synchronized String getValueOfDimension(KPIInformationElement elem) {
        KPIInformationConfig temp = (KPIInformationConfig) kpiInformationConfigs.get(elem);
        if (temp == null && strategy != null && strategy.getGrlspec() != null && strategy.getGrlspec().getUrnspec() != null) {
            temp = (KPIInformationConfig) ModelCreationFactory.getNewObject(strategy.getGrlspec().getUrnspec(), KPIInformationConfig.class);
            kpiInformationConfigs.put(elem, temp);
            return temp.getValueOfDimension();
        } else if (temp != null) {
            return temp.getValueOfDimension();
        } else
            return ""; //$NON-NLS-1$

    }

    public synchronized Evaluation getEvaluationObject(IntentionalElement elem) {
        Evaluation temp = (Evaluation) evaluations.get(elem);
        // if the evaluation is null, it is a new element and we need to create a new evaluation
        if (temp == null && strategy != null && strategy.getGrlspec() != null && strategy.getGrlspec().getUrnspec() != null) {
            temp = (Evaluation) ModelCreationFactory.getNewObject(strategy.getGrlspec().getUrnspec(), Evaluation.class);
            evaluations.put(elem, temp);
        }
        return temp;
    }

    public synchronized boolean isIgnored(IntentionalElement elem) {
    	if( this.hasSpecificMetadata( elem ) ) {
    		return true;    					
    	} else if( elem.getType() == IntentionalElementType.INDICATOR_LITERAL ){
    		for( Iterator iter = elem.getLinksSrc().iterator(); iter.hasNext(); ) {
    			ElementLink el = (ElementLink) iter.next();
    			if( el.getDest() instanceof IntentionalElement ) {
    				if( this.hasSpecificMetadata( (IntentionalElement) el.getDest() ))
    					return true;
    			}
    		}
    	}
    	return false;
    }
    
    private synchronized boolean hasSpecificMetadata(IntentionalElement elem) {
    	if( elem.getMetadata().size() > 0 ) {
    		for( Iterator iter = elem.getMetadata().iterator(); iter.hasNext();) {
    			Metadata md = (Metadata) iter.next();	
    			if(md.getName().equalsIgnoreCase( "ST_Legal" )){ //$NON-NLS-1$
    				if(md.getValue().equalsIgnoreCase( "No" )) //$NON-NLS-1$
    					return true;
    			} else if( md.getName().equals(Messages.getString("ConditionalGRLStrategyAlgorithm_IgnoreNode")) ) { //$NON-NLS-1$
    				return true;
    			}
    		}    		
    	}
    	return false;    	
    	
    }
    
    public synchronized boolean isConditionResource(IntentionalElement elem) {
    	if( elem.getType() == IntentionalElementType.RESSOURCE_LITERAL ) {
    		for( Iterator iter = elem.getMetadata().iterator(); iter.hasNext();) {
    			Metadata md = (Metadata) iter.next();	
    			if(md.getName().equalsIgnoreCase( "ST_CONDITIONTYPE" )){ //$NON-NLS-1$
    				return true;
    			}
    		}
    	}
    	return false; 
    }
    
    public synchronized KPIInformationConfig getKPIInformationConfigObject(KPIInformationElement elem) {
        KPIInformationConfig temp = (KPIInformationConfig) kpiInformationConfigs.get(elem);
        // if the KPIInformationConfig is null, it is a new element and we need to create a new KPIInformationConfig
        if (temp == null && strategy != null && strategy.getGrlspec() != null && strategy.getGrlspec().getUrnspec() != null) {
            temp = (KPIInformationConfig) ModelCreationFactory.getNewObject(strategy.getGrlspec().getUrnspec(), KPIInformationConfig.class);
            kpiInformationConfigs.put(elem, temp);
        }
        return temp;
    }

    public synchronized EvaluationStrategy getEvaluationStrategy() {
        return strategy;
    }

    public synchronized void setEvaluationForElement(IntentionalElement element, Evaluation eval) {
        if (strategy != null) {
            evaluations.remove(element);
            if (eval != null)
                evaluations.put(element, eval);
            calculateEvaluation();
        }

    }

    public synchronized void setKPIInformationConfigForElement(KPIInformationElement element, KPIInformationConfig config) {
        if (config != null) {
            kpiInformationConfigs.remove(element);
            kpiInformationConfigs.put(element, config);
            calculateEvaluation();
        }

    }

    public synchronized void setStrategy(EvaluationStrategy strategy) {
        this.strategy = strategy;

        // Create a new hash map for the evaluation of this strategy
        evaluations = new HashMap();

        // Create a new hash map for the KPIInformationConfig this strategy
        kpiInformationConfigs = new HashMap();

        if (strategy != null) {
            // Go through all the intentionalElement and create a new Evaluation object if no one exist for this strategy
            GRLspec grl = strategy.getGrlspec();
            Iterator it = grl.getIntElements().iterator();
            while (it.hasNext()) {
                IntentionalElement elem = (IntentionalElement) it.next();
                // Verify if an evaluation exist for this strategy. This could create performance problem!!!!
                Iterator sc = strategy.getEvaluations().iterator();
                Evaluation eval = null;
                while (sc.hasNext() && eval == null) {
                    Evaluation temp = (Evaluation) sc.next();
                    if (temp.getIntElement() == elem) {
                        eval = temp;
                    }
                }
                if (eval == null) {
                    eval = (Evaluation) ModelCreationFactory.getNewObject(grl.getUrnspec(), Evaluation.class);
                }
                evaluations.put(elem, eval);
            }

            // Go through all the KPIInformationElement and create a new KPIInformationConfig object if no one exist for this strategy
            grl = strategy.getGrlspec();
            it = grl.getKpiInformationElements().iterator();
            while (it.hasNext()) {
                KPIInformationElement elem = (KPIInformationElement) it.next();
                // Verify if an KPIInformationConfig exist for this strategy. This could create performance problem!!!!
                Iterator sc = strategy.getKpiInfoConfig().iterator();
                KPIInformationConfig config = null;
                while (sc.hasNext() && config == null) {
                    KPIInformationConfig temp = (KPIInformationConfig) sc.next();
                    if (temp.getKpiInfoElement() == elem) {
                        config = temp;
                    }
                }
                if (config == null) {
                    config = (KPIInformationConfig) ModelCreationFactory.getNewObject(grl.getUrnspec(), KPIInformationConfig.class);
                }
                kpiInformationConfigs.put(elem, config);
            }

            calculateEvaluation();
        }

        // Refresh the kpi list view
        if (kpiListViewer != null) {
            EditPart ep = kpiListViewer.getContents();
            if (ep instanceof KPIRootEditPart) {
                ((KPIRootEditPart) ep).refreshIndicatorTreeEditPart(ep);
            }
        }
    }

    /**
     * Sets the quantitative importance of an intentional element
     * 
     * @param element
     *            the element to apply the new value to
     * @param value
     *            the new quantitative importance value
     * 
     */
    public synchronized void setIntentionalElementQuantitativeImportance(IntentionalElement element, int value) {
        // The importance should be between 0 and 100
        if (value <= 100 && value >= 0) {

            // Change the value in the evaluation
            if (value != element.getImportanceQuantitative()) {
                element.setImportanceQuantitative(value);
                syncIntentionalElementQuantitativeImportance(element, value);
            }

            // do we have a command for this?
            calculateEvaluation();
        }
    }

    /**
     * Synchronize the qualitative importance to the quantitative importance
     * 
     * @param element
     *            intentional element model for the intentional element being synchronized
     * @param value
     *            quantitative value for the intentional element
     * 
     */
    private void syncIntentionalElementQuantitativeImportance(IntentionalElement element, int value) {
        ImportanceType label = element.getImportance();

        ImportanceType toSet = getQualitativeImportanceForQuantitativeValue(value);

        if (!label.equals(toSet))
            element.setImportance(toSet);

    }

    public static ImportanceType getQualitativeImportanceForQuantitativeValue(int value) {
        if (value >= 66 && value <= 100)
            return ImportanceType.HIGH_LITERAL;
        else if (value >= 33 && value < 66)
            return ImportanceType.MEDIUM_LITERAL;
        else if (value > 0 && value < 33)
            return ImportanceType.LOW_LITERAL;
        else if (value == 0)
            return ImportanceType.NONE_LITERAL;
        return null;
    }

    /**
     * Sets the qualitative importance
     * 
     * @param element
     *            the element to which to apply the new value
     * @param value
     *            the new qualitative importance value
     * 
     */
    public synchronized void setIntentionalElementQualitativeImportance(IntentionalElement element, ImportanceType value) {
        // Change the value in the evaluation
        if (value != element.getImportance()) {
            element.setImportance(value);
            syncIntentionalElementQualitativeImportance(element);

        }
        // do we have a command for this?
        calculateEvaluation();
    }

    /**
     * Synchronizes the quantitative evaluation to the qualitative evaluation
     * 
     * @param element
     *            the intentional element being synchronized
     * 
     */
    private void syncIntentionalElementQualitativeImportance(IntentionalElement element) {
        String type = element.getImportance().getName();
        int importance = element.getImportanceQuantitative();

        if (ImportanceType.HIGH_LITERAL.getName().equals(type) && (importance < 66))
            element.setImportanceQuantitative(100);
        else if (ImportanceType.MEDIUM_LITERAL.getName().equals(type) && (importance < 33 || importance >= 66))
            element.setImportanceQuantitative(50);
        else if (ImportanceType.LOW_LITERAL.getName().equals(type) && (importance == 0 || importance >= 33))
            element.setImportanceQuantitative(25);
        else if (ImportanceType.NONE_LITERAL.getName().equals(type) && (importance > 0))
            element.setImportanceQuantitative(0);

    }

    /**
     * Sets the quantitative evaluation
     * 
     * @param element
     *            the element to apply the new value to
     * @param value
     *            the new quantitative evaluation value
     * @param delete
     *            Are we removing this evaluation or adding it?
     * 
     */
    protected synchronized void setIntentionalElementEvaluation(IntentionalElement element, int value, boolean delete) {
        // The evaluation could only be between 100 and -100. Do nothing if it is not the case
        if (value <= IGRLStrategyAlgorithm.SATISFICED && value >= IGRLStrategyAlgorithm.UNDECIDED) {
            Evaluation eval = (Evaluation) evaluations.get(element);
            // Change the value in the evaluation
            if (value != eval.getEvaluation()) {
                eval.setEvaluation(value);
                syncIntentionalElementQualitativeEvaluation(eval, value);
                setEvaluationMetadata(element, eval);
            }
            if(!delete) {
                // If it is a new Evaluation entered by the user, link it with the strategy and intentionalElement
                AddEvaluationCommand cmd = new AddEvaluationCommand(eval, element, strategy);
                execute(cmd);
            }
            else {
                DeleteEvaluationCommand cmd = new DeleteEvaluationCommand(eval);
                execute(cmd);
            }

            calculateEvaluation();
        }
    }
    
    /**
     * Sets the quantitative evaluation
     * @param element
     *              the element to apply the new value to
     * @param value
     *              the new quantitative evaluation value
     */
    public synchronized void setIntentionalElementEvaluation(IntentionalElement element, int value) {
        setIntentionalElementEvaluation(element, value, false);
    }
    
    /**
     * Remove a quantitative evaluation
     * @param element
     *              the element to remove the evaluation
     * @param oldValue
     *              the value that was there before
     */
    public synchronized void removeIntentionalElementEvaluation(IntentionalElement element, int oldValue) {
        setIntentionalElementEvaluation(element, oldValue, true);
    }

    private void execute(Command cmd) {
        if (cmd.canExecute()) {
            // bug 717 original code
            cmd.execute();

            /*
             * proposed enh - partial solution only. see comments in bug. if (multieditor==null )// won't be able to undo cmd.execute(); else
             * multieditor.getDelegatingCommandStack().execute(cmd);
             */
        }
    }

    /**
     * Synchronize the qualitative evaluation to the quantitative evaluation
     * 
     * @param eval
     *            evaluation model for the intentional element being synchronized
     * @param value
     *            quantitative value for the intentional element
     * 
     */
    private void syncIntentionalElementQualitativeEvaluation(Evaluation eval, int value) {
        QualitativeLabel label = eval.getQualitativeEvaluation();

        QualitativeLabel toSet = getQualitativeEvaluationForQuantitativeValue(value);

        if (!label.equals(toSet))
            eval.setQualitativeEvaluation(toSet);

    }

    public static QualitativeLabel getQualitativeEvaluationForQuantitativeValue(int value) {
        if (value == IGRLStrategyAlgorithm.SATISFICED)
            return QualitativeLabel.SATISFIED_LITERAL;
        else if (value > IGRLStrategyAlgorithm.NONE && value < IGRLStrategyAlgorithm.SATISFICED)
            return QualitativeLabel.WEAKLY_SATISFIED_LITERAL;
        else if (value > IGRLStrategyAlgorithm.DENIED && value < IGRLStrategyAlgorithm.NONE)
            return QualitativeLabel.WEAKLY_DENIED_LITERAL;
        else if (value == IGRLStrategyAlgorithm.DENIED)
            return QualitativeLabel.DENIED_LITERAL;
        else if (value == IGRLStrategyAlgorithm.NONE)
            return QualitativeLabel.NONE_LITERAL;
        else if (value == IGRLStrategyAlgorithm.CONFLICT)
            return QualitativeLabel.CONFLICT_LITERAL;
        else if (value == IGRLStrategyAlgorithm.UNDECIDED)
            return QualitativeLabel.UNKNOWN_LITERAL;
        return null;
    }

    /**
     * Sets the qualitative evaluation
     * 
     * @param element
     *            the element to which to apply the new value
     * @param value
     *            the new qualitative evaluation value
     * 
     */
    public synchronized void setIntentionalElementQualitativeEvaluation(IntentionalElement element, QualitativeLabel value) {
        Evaluation eval = (Evaluation) evaluations.get(element);
        // Change the value in the evaluation
        if (value != eval.getQualitativeEvaluation()) {
            eval.setQualitativeEvaluation(value);
            syncIntentionalElementEvaluation(eval);
            setEvaluationMetadata(element, eval);
        }
        // If it is a new Evaluation enter by the user, link it with the strategy and intentionalElement
        AddEvaluationCommand cmd = new AddEvaluationCommand(eval, element, strategy);
        execute(cmd);

        calculateEvaluation();
    }

    /**
     * Synchronizes the quantitative evaluation to the qualitative evaluation
     * 
     * @param eval
     *            evaluation model for the intentional element being synchronized
     * 
     */
    private void syncIntentionalElementEvaluation(Evaluation eval) {
        String type = eval.getQualitativeEvaluation().getName();
        int evaluation = eval.getEvaluation();

        if (QualitativeLabel.SATISFIED_LITERAL.getName().equals(type) && evaluation < IGRLStrategyAlgorithm.SATISFICED)
            eval.setEvaluation(IGRLStrategyAlgorithm.SATISFICED);
        else if (QualitativeLabel.WEAKLY_SATISFIED_LITERAL.getName().equals(type)
                && (evaluation <= IGRLStrategyAlgorithm.NONE || evaluation == IGRLStrategyAlgorithm.SATISFICED))
            eval.setEvaluation(IGRLStrategyAlgorithm.WSATISFICED);
        else if (QualitativeLabel.WEAKLY_DENIED_LITERAL.getName().equals(type)
                && (evaluation >= IGRLStrategyAlgorithm.NONE || evaluation == IGRLStrategyAlgorithm.DENIED))
            eval.setEvaluation(IGRLStrategyAlgorithm.WDENIED);
        else if (QualitativeLabel.DENIED_LITERAL.getName().equals(type) && evaluation != IGRLStrategyAlgorithm.DENIED)
            eval.setEvaluation(IGRLStrategyAlgorithm.DENIED);
        else if (QualitativeLabel.NONE_LITERAL.getName().equals(type) && evaluation != IGRLStrategyAlgorithm.NONE)
            eval.setEvaluation(IGRLStrategyAlgorithm.NONE);
        else if (QualitativeLabel.CONFLICT_LITERAL.getName().equals(type) && evaluation != IGRLStrategyAlgorithm.CONFLICT)
            eval.setEvaluation(IGRLStrategyAlgorithm.CONFLICT);
        else if (QualitativeLabel.UNKNOWN_LITERAL.getName().equals(type) && evaluation != IGRLStrategyAlgorithm.UNDECIDED)
            eval.setEvaluation(IGRLStrategyAlgorithm.UNDECIDED);

    }

    /*
     * KPI management methods.
     */

    public synchronized void setLevelOfDimension(KPIInformationElement element, String value) {
        if (value != null) {
            KPIInformationConfig config = (KPIInformationConfig) kpiInformationConfigs.get(element);
            // Change the value in the KPIInformationConfig
            if (!value.equals(config.getLevelOfDimension())) {
                config.setLevelOfDimension(value);
            }
            // If it is a new KPIInformationConfig enter by the user, link it with the strategy and KPIInformationElement
            AddKPIInformationConfigCommand cmd = new AddKPIInformationConfigCommand(config, element, strategy);
            execute(cmd);

            // In this version, KPI Information Element is not involved in the evaluation
            // calculateEvaluation();
        }
    }

    public synchronized void setValueOfDimension(KPIInformationElement element, String value) {
        if (value != null) {
            KPIInformationConfig config = (KPIInformationConfig) kpiInformationConfigs.get(element);
            // Change the value in the KPIInformationConfig
            if (!value.equals(config.getValueOfDimension())) {
                config.setValueOfDimension(value);
            }
            // If it is a new KPIInformationConfig enter by the user, link it with the strategy and KPIInformationElement
            AddKPIInformationConfigCommand cmd = new AddKPIInformationConfigCommand(config, element, strategy);
            execute(cmd);

            // In this version, KPI Information Element is not involved in the evaluation
            // calculateEvaluation();
        }
    }

    public synchronized void setKPIEvalValueSet(IntentionalElement element, EStructuralFeature feature, double value) {
        Evaluation eval = (Evaluation) evaluations.get(element);
        KPIEvalValueSet kpiEval = eval.getKpiEvalValueSet();

        if (feature.getName().equals("targetValue")) { //$NON-NLS-1$
            kpiEval.setTargetValue(value);
        } else if (feature.getName().equals("thresholdValue")) { //$NON-NLS-1$
            kpiEval.setThresholdValue(value);
        } else if (feature.getName().equals("worstValue")) { //$NON-NLS-1$
            kpiEval.setWorstValue(value);
        } else if (feature.getName().equals("evaluationValue")) { //$NON-NLS-1$
            kpiEval.setEvaluationValue(value);
        }

        // If it is a new Evaluation enter by the user, link it with the strategy and intentionalElement
        AddEvaluationCommand cmd = new AddEvaluationCommand(eval, element, strategy);
        execute(cmd);

        calculateIndicatorEvalLevel(eval);
        calculateEvaluation();
    }

    public synchronized void resetKPIEvalValueSet(IntentionalElement element) {
        Evaluation eval = (Evaluation) evaluations.get(element);
        KPIEvalValueSet kpiEval = eval.getKpiEvalValueSet();
        double defaultValue = 0.0;

        kpiEval.setTargetValue(defaultValue);
        kpiEval.setThresholdValue(defaultValue);
        kpiEval.setWorstValue(defaultValue);
        kpiEval.setEvaluationValue(defaultValue);

        // If it is a new Evaluation enter by the user, link it with the strategy and intentionalElement
        AddEvaluationCommand cmd = new AddEvaluationCommand(eval, element, strategy);
        execute(cmd);

        calculateIndicatorEvalLevel(eval);
        calculateEvaluation();
    }

    public synchronized void setKPIEvalValueSetUnit(IntentionalElement element, EStructuralFeature feature, String value) {
        Evaluation eval = (Evaluation) evaluations.get(element);
        KPIEvalValueSet kpiEval = eval.getKpiEvalValueSet();

        if (feature.getName().equals("unit")) { //$NON-NLS-1$
            kpiEval.setUnit(value);
        }
    }

    public synchronized void calculateIndicatorEvalLevel(Evaluation eval) {
        KPIEvalValueSet kpiEval = eval.getKpiEvalValueSet();
        double evalLevel;

        if (kpiEval.getTargetValue() < kpiEval.getWorstValue()) {
            if (kpiEval.getEvaluationValue() <= kpiEval.getTargetValue()) {
                evalLevel = 100;
            } else if (kpiEval.getEvaluationValue() >= kpiEval.getWorstValue()) {
                evalLevel = -100;
            } else if (kpiEval.getEvaluationValue() < kpiEval.getThresholdValue()) {
                evalLevel = Math.abs(kpiEval.getEvaluationValue() - kpiEval.getThresholdValue())
                        / Math.abs(kpiEval.getTargetValue() - kpiEval.getThresholdValue()) * 100;
            } else {
                evalLevel = Math.abs(kpiEval.getEvaluationValue() - kpiEval.getThresholdValue())
                        / Math.abs(kpiEval.getThresholdValue() - kpiEval.getWorstValue()) * -100;
            }
        } else {
            if (kpiEval.getEvaluationValue() >= kpiEval.getTargetValue()) {
                evalLevel = 100;
            } else if (kpiEval.getEvaluationValue() <= kpiEval.getWorstValue()) {
                evalLevel = -100;
            } else if (kpiEval.getEvaluationValue() >= kpiEval.getThresholdValue()) {
                evalLevel = Math.abs(kpiEval.getEvaluationValue() - kpiEval.getThresholdValue())
                        / Math.abs(kpiEval.getTargetValue() - kpiEval.getThresholdValue()) * 100;
            } else {
                evalLevel = Math.abs(kpiEval.getEvaluationValue() - kpiEval.getThresholdValue())
                        / Math.abs(kpiEval.getThresholdValue() - kpiEval.getWorstValue()) * -100;
            }
        }

        eval.setEvaluation((int) evalLevel);
    }

    public synchronized void setMultieditor(UCMNavMultiPageEditor multieditor) {
        this.multieditor = multieditor;
    }

    public void setKPIViewer(ScrollingGraphicalViewer viewer) {
        this.kpiViewer = viewer;
    }

    public void setKPIListViewer(TreeViewer viewer) {
        this.kpiListViewer = viewer;
    }

    // Uses metadata attached to intentional elements and actors to store run-time evaluations.
    // Enables OCL constraints to reason about satisfaction levels for a strategy.

    /**
     * Sets _numEval and _qualEval metadata for the intentional element
     */
    private void setEvaluationMetadata(IntentionalElement element, Evaluation eval) {
        String numEvalAsString = Integer.toString(eval.getEvaluation());
        String qualEvalAsString = eval.getQualitativeEvaluation().toString();

        Metadata metaNumerical = MetadataHelper.getMetaDataObj(element, METADATA_NUMEVAL);
        if (metaNumerical != null) {
            // Run-time metadata already exist for this element
            metaNumerical.setValue(numEvalAsString);
        } else {
            // Add new run-time metadata for this element
            URNspec urnSpec = element.getGrlspec().getUrnspec();
            MetadataHelper.addMetaData(urnSpec, element, METADATA_NUMEVAL, numEvalAsString);
        }

        Metadata metaQuantitative = MetadataHelper.getMetaDataObj(element, METADATA_QUALEVAL);
        if (metaQuantitative != null) {
            // Run-time metadata already exist for this element
            metaQuantitative.setValue(qualEvalAsString);
        } else {
            // Add new run-time metadata for this element
            URNspec urnSpec = element.getGrlspec().getUrnspec();
            MetadataHelper.addMetaData(urnSpec, element, METADATA_QUALEVAL, qualEvalAsString);
        }
    }

    /**
     * Sets _numEval and _qualEval metadata for the actor
     */
    private void setEvaluationMetadata(Actor actor, int actorEval) {
        String numEvalAsString = Integer.toString(actorEval);
        if (getQualitativeEvaluationForQuantitativeValue(actorEval)==null)return;
        String qualEvalAsString = getQualitativeEvaluationForQuantitativeValue(actorEval).toString();

        Metadata metaNumerical = MetadataHelper.getMetaDataObj(actor, METADATA_NUMEVAL);
        if (metaNumerical != null) {
            // Run-time metadata already exists for this element
            metaNumerical.setValue(numEvalAsString);
        } else {
            // Add new run-time metadata for this element
            URNspec urnSpec = actor.getGrlspec().getUrnspec();
            MetadataHelper.addMetaData(urnSpec, actor, METADATA_NUMEVAL, numEvalAsString);
        }

        Metadata metaQuantitative = MetadataHelper.getMetaDataObj(actor, METADATA_QUALEVAL);
        if (metaQuantitative != null) {
            // Run-time metadata already exists for this element
            metaQuantitative.setValue(qualEvalAsString);
        } else {
            // Add new run-time metadata for this element
            URNspec urnSpec = actor.getGrlspec().getUrnspec();
            MetadataHelper.addMetaData(urnSpec, actor, METADATA_QUALEVAL, qualEvalAsString);
        }
    }

    public synchronized void startDifferenceMode( EvaluationStrategy strategy ) {
    	strategy1 = strategy;
    	differenceMode = true;
    }
    
    public void setComparisonStrategy( EvaluationStrategy strategy ) {
    	if( differenceMode && strategy != null && strategy1 != null && strategy != strategy1 ) {
    		strategy2 = strategy;
    		calculateEvaluation();
    	}
    }

    public synchronized void stopDifferenceMode() {
    	strategy = strategy1;
    	strategy1 = null;
    	strategy2 = null;
    	differenceMode = false;
    }
    
    public synchronized boolean isDifferenceMode() {
    	return( differenceMode );
    }
    
    public boolean isDifferenceMode( EvaluationStrategy strategy ) {
    	
    	if( JUCMNavPlugin.isInDebug() ){
    		if( strategy1 != null )
    			System.out.println( "\n\nisDifferenceMode strategy1: " + strategy1.getName() ); //$NON-NLS-1$
    		if( strategy2 != null )
    			System.out.println( "isDifferenceMode " + " strategy2: " + strategy2.getName() ); //$NON-NLS-1$
    		if( strategy != null )
    			System.out.println( "isDifferenceMode strategy: " + strategy.getName() ); //$NON-NLS-1$
    	}

    	
    	return( differenceMode && strategy != null && strategy != strategy1 && strategy != strategy2 );
    }
    
}
