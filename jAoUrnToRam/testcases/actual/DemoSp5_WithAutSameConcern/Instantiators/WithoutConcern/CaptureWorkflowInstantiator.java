package DemoSp5_WithAutSameConcern.Instantiators.WithoutConcern;

import ram.reactiveworkflow.*;
import ram.workflow.*;

public class CaptureWorkflowInstantiator extends WorkflowInstantiator{
    public Workflow workflow=new Workflow();
    public ConditionalExecutionNode _OrFork70=new ConditionalExecutionNode();
    public StubNode _Aut3=new StubNode();
    public EndNode _Captured=new EndNode();
    public StartupNode _Capture=new StartupNode();
    public CustomizableNode _ValidateWitnessInfo=createCustomizableNode("DemoSp5_WithAutSameConcern.Steps.WithoutConcern.CaptureLocationType_CaptureWitnessInfo_ValidateWitness_LoginInfo.ValidateWitnessInfo");
    public StubNode _Aut2=new StubNode();
    public CustomizableNode _PhoneDataRequest=createCustomizableNode("DemoSp5_WithAutSameConcern.Steps.WithoutConcern.CaptureLocationType_CaptureWitnessInfo_ValidateWitness_LoginInfo.PhoneDataRequest");
    public InputNode _ValidateWitness=new InputNode();
    public CustomizableNode _FakeCrisisNotification=createCustomizableNode("DemoSp5_WithAutSameConcern.Steps.WithoutConcern.CaptureLocationType_CaptureWitnessInfo_ValidateWitness_LoginInfo.FakeCrisisNotification");
    public EndNode _FakeCrisis=new EndNode();
    public InputNode _CaptureWitnessInfo=new InputNode();
    public StubNode _Aut1=new StubNode();
    public CustomizableNode _CrisisInfoRequest=createCustomizableNode("DemoSp5_WithAutSameConcern.Steps.WithoutConcern.CaptureLocationType_CaptureWitnessInfo_ValidateWitness_LoginInfo.CrisisInfoRequest");
    public InputNode _CaptureLocationType=new InputNode();
    public ConditionalExecutionNode _IsPhoneCompanyAvailable=new ConditionalExecutionNode();
    public CustomizableNode _ProcessValidateWitness=createCustomizableNode("DemoSp5_WithAutSameConcern.Steps.WithoutConcern.CaptureLocationType_CaptureWitnessInfo_ValidateWitness_LoginInfo.ProcessValidateWitness");
    public CustomizableNode _ProcessCaptureWitnessInfo=createCustomizableNode("DemoSp5_WithAutSameConcern.Steps.WithoutConcern.CaptureLocationType_CaptureWitnessInfo_ValidateWitness_LoginInfo.ProcessCaptureWitnessInfo");
    public CustomizableNode _ProcessCaptureLocationType=createCustomizableNode("DemoSp5_WithAutSameConcern.Steps.WithoutConcern.CaptureLocationType_CaptureWitnessInfo_ValidateWitness_LoginInfo.ProcessCaptureLocationType");

    public void link(){
        linkNodesToNextNodes();
        linkNodesToWorkflow();
        linkStartNodesToWorkflow();
    }

    public void linkNodesToNextNodes(){
        _OrFork70.addNextNode("witnessInfoNotVerified",_FakeCrisisNotification);
        _OrFork70.addNextNode("else",_Captured);
        _Aut3.addNextNode("_OUT1",_OrFork70);
        _Capture.addNextNode(_CaptureWitnessInfo);
        _ValidateWitnessInfo.addNextNode(_OrFork70);
        _Aut2.addNextNode("_OUT1",_ValidateWitnessInfo);
        _PhoneDataRequest.addNextNode(_ValidateWitness);
        _ValidateWitness.addNextNode(_ProcessValidateWitness);
        _FakeCrisisNotification.addNextNode(_FakeCrisis);
        _CaptureWitnessInfo.addNextNode(_ProcessCaptureWitnessInfo);
        _Aut1.addNextNode("_OUT1",_IsPhoneCompanyAvailable);
        _CrisisInfoRequest.addNextNode(_CaptureLocationType);
        _CaptureLocationType.addNextNode(_ProcessCaptureLocationType);
        _IsPhoneCompanyAvailable.addNextNode("yes",_PhoneDataRequest);
        _IsPhoneCompanyAvailable.addNextNode("no",_CrisisInfoRequest);
        _ProcessValidateWitness.addNextNode(_Aut2,"Aut2_IN1");
        _ProcessCaptureWitnessInfo.addNextNode(_Aut1,"Aut1_IN1");
        _ProcessCaptureLocationType.addNextNode(_Aut3,"Aut3_IN1");
    }

    public void linkNodesToWorkflow(){
        workflow.addNode(_OrFork70);
        workflow.addNode(_Aut3);
        workflow.addNode(_Captured);
        workflow.addNode(_Capture);
        workflow.addNode(_ValidateWitnessInfo);
        workflow.addNode(_Aut2);
        workflow.addNode(_PhoneDataRequest);
        workflow.addNode(_ValidateWitness);
        workflow.addNode(_FakeCrisisNotification);
        workflow.addNode(_FakeCrisis);
        workflow.addNode(_CaptureWitnessInfo);
        workflow.addNode(_Aut1);
        workflow.addNode(_CrisisInfoRequest);
        workflow.addNode(_CaptureLocationType);
        workflow.addNode(_IsPhoneCompanyAvailable);
        workflow.addNode(_ProcessValidateWitness);
        workflow.addNode(_ProcessCaptureWitnessInfo);
        workflow.addNode(_ProcessCaptureLocationType);
    }

    public void linkStartNodesToWorkflow(){
        workflow.addStartupNode(_Capture,false);
    }

    public void bind(DemoSp5_WithAutSameConcern.Instantiators.WithoutConcern.AuthenticationWorkflowInstantiator p_Authentication){

        Binding Aut3_Authentication_PluginBinding=new Binding(p_Authentication.workflow);
        _Aut3.addBinding(Aut3_Authentication_PluginBinding);
        _Aut3.addInBinding(Aut3_Authentication_PluginBinding,"Aut3_IN1",p_Authentication._IsAlreadyAuthenticated);
        _Aut3.addOutBinding(Aut3_Authentication_PluginBinding,p_Authentication._AuthenticationVerified,"Aut3_OUT1");

        Binding Aut2_Authentication_PluginBinding=new Binding(p_Authentication.workflow);
        _Aut2.addBinding(Aut2_Authentication_PluginBinding);
        _Aut2.addInBinding(Aut2_Authentication_PluginBinding,"Aut2_IN1",p_Authentication._IsAlreadyAuthenticated);
        _Aut2.addOutBinding(Aut2_Authentication_PluginBinding,p_Authentication._AuthenticationVerified,"Aut2_OUT1");

        Binding Aut1_Authentication_PluginBinding=new Binding(p_Authentication.workflow);
        _Aut1.addBinding(Aut1_Authentication_PluginBinding);
        _Aut1.addInBinding(Aut1_Authentication_PluginBinding,"Aut1_IN1",p_Authentication._IsAlreadyAuthenticated);
        _Aut1.addOutBinding(Aut1_Authentication_PluginBinding,p_Authentication._AuthenticationVerified,"Aut1_OUT1");
    }
}

