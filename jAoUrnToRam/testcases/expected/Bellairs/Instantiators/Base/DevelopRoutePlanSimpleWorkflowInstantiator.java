package Bellairs.Instantiators.Base;

import ram.reactiveworkflow.*;
import ram.workflow.*;

public class DevelopRoutePlanSimpleWorkflowInstantiator extends WorkflowInstantiator{
    public Workflow workflow=new Workflow();
    public StartupNode _EstablishRoutes=new StartupNode();
    public EndNode _RoutesEstablished=new EndNode();
    public InputNode _RequestDriverConfirmation=new InputNode();
    public CustomizableNode _ShowVehicleList=createCustomizableNode("Bellairs.Steps.Base.EstablishRoutes.ShowVehicleList");
    public CustomizableNode _ShowAssignment=createCustomizableNode("Bellairs.Steps.Base.RequestDriverConfirmation.ShowAssignment");
    public StubNode _AspectMarker117512=new StubNode();

    public void link(){
        linkNodesToNextNodes();
        linkNodesToWorkflow();
        linkStartNodesToWorkflow();
    }

    public void linkNodesToNextNodes(){
        _EstablishRoutes.addNextNode(_ShowVehicleList);
        _RequestDriverConfirmation.addNextNode(_AspectMarker117512,"1");
        _ShowVehicleList.addNextNode(_RequestDriverConfirmation);
        _ShowAssignment.addNextNode(_RoutesEstablished);
        _AspectMarker117512.addNextNode("1",_ShowAssignment);
    }

    public void linkNodesToWorkflow(){
        workflow.addNode(_EstablishRoutes);
        workflow.addNode(_RoutesEstablished);
        workflow.addNode(_RequestDriverConfirmation);
        workflow.addNode(_ShowVehicleList);
        workflow.addNode(_ShowAssignment);
        workflow.addNode(_AspectMarker117512);
    }

    public void linkStartNodesToWorkflow(){
        workflow.addStartupNode(_EstablishRoutes,false);
    }

    public void bind(Bellairs.Instantiators.Authentication.AuthenticationWorkflowInstantiator p_Authentication){

        Binding l_AspectMarker117512_PluginBinding=new Binding(p_Authentication.workflow);
        _AspectMarker117512.addBinding(l_AspectMarker117512_PluginBinding);
        _AspectMarker117512.addInBinding(l_AspectMarker117512_PluginBinding,"1",p_Authentication._CheckAuthentication);
        _AspectMarker117512.addOutBinding(l_AspectMarker117512_PluginBinding,p_Authentication._Authenticated,"1");
    }
}
