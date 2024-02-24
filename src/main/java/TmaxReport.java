// Simcenter STAR-CCM+ macro: raports.java
// Ciobanu Dragos 24.02.2024
// tool description: generate temperature maximum report and monitor for the parts chosen by the user, for EMAG part groups all reports and derivate monitors in a specific group called EMAG

//next steps - folder group for Solid and fluid


import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;

import star.common.*;
import star.base.neo.*;
import star.base.report.*;
import star.coremodule.objectselector.*;

public class TmaxReport extends StarMacro {

     private static Simulation sim;

    public void execute()  {

        sim = getActiveSimulation();

        setFoldersGroup();

        generateReports();

        setVolumeMeshRepresentation();

        generateMonitors();


    }


    //the following method will generate reports based on the collection of parts chose by user
    private static void generateReports() {

        Collection<GeometryPart> selectParts = new ArrayList<>();
        selectParts =(Collection<GeometryPart>) getSelectedObjects(sim, "Select Parts", FilterModel.AllGeometryPartsFilterModel, ModelDescriptor.SelectionType.Multiple);


        for (GeometryPart part : selectParts) {

            MaxReport maxReportGenerate =
                    sim.getReportManager().createReport(MaxReport.class);

            maxReportGenerate.setPresentationName("max_temperature"+"_"+ part.getPresentationName());

            PrimitiveFieldFunction maxTemperature =
                    ((PrimitiveFieldFunction) sim.getFieldFunctionManager().getFunction("Temperature"));

            maxReportGenerate.setFieldFunction(maxTemperature);

            maxReportGenerate.getParts().setObjects(part);

            if(part.getPresentationName().contains("EMAG")){

                ((ClientServerObjectGroup) sim.getReportManager().getGroupsManager().getObject("EMAG")).getGroupsManager().groupObjects("EMAG", new NeoObjectVector(new Object[] {maxReportGenerate}), true);


            }



        }

        sim.println(selectParts.size());
    }

    //JFrame for user interaction
    public static Collection<? extends ClientServerObject> getSelectedObjects(Simulation sim, String message, FilterModel filterModel, ModelDescriptor.SelectionType type) {
        ModelDescriptor descriptor = new SelectorDescriptor.Builder(sim, filterModel).selectionType(type).build();
        ObjectSelector selector = new ObjectSelector(descriptor);

        // Define frame, run the object selector
        JFrame parent = new JFrame("Selection pane");
        JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(new Object[]{selector});
        //optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        optionPane.setOptionType(JOptionPane.PLAIN_MESSAGE);

        JDialog dialog = optionPane.createDialog(parent, message);
        dialog.setVisible(true);

        Collection<NamedObject> selected = SelectorUtils.createObjects(sim, selector.getSelected());

        return selected;
    }



    //the following method sets the folders group for reports and monitors
    private static void setFoldersGroup() {


        if(!sim.getReportManager().getGroupsManager().has("EMAG")){

        sim.getReportManager().getGroupsManager().createGroup("New Group");

        ((ClientServerObjectGroup) sim.getReportManager().getGroupsManager().getObject("New Group")).setPresentationName("EMAG");
        }


        if(!sim.getMonitorManager().getGroupsManager().has("EMAG")){

            sim.getMonitorManager().getGroupsManager().createGroup("New Group");

            ((ClientServerObjectGroup) sim.getMonitorManager().getGroupsManager().getObject("New Group")).setPresentationName("EMAG");
        }


    }


    //the following method will set the representation to Volume Mesh for all the reports present in the simulation, including those that exist before running the script
    private void setVolumeMeshRepresentation() {

        Simulation sim = getActiveSimulation();
        FvRepresentation fvRepresentation_0 = ((FvRepresentation) sim.getRepresentationManager().getObject("Volume Mesh")); //Declare Volume Mesh representation
        sim.getReportManager().applyRepresentation(fvRepresentation_0); //Set Volume Mesh representation
    }

    //the following method will generate monitors according to user selection made for reports
    private static void generateMonitors() {

        Collection<Report> selectReports = new ArrayList<>();

        selectReports =  sim.getReportManager().getObjects().stream().filter(report -> report.getPresentationName().contains("EMAG")).collect(Collectors.toList());

        sim.println(selectReports.size());


        for ( Report report : selectReports) {

            if(report.getPresentationName().contains("EMAG")){

            ReportMonitor monitor  = report.createMonitor();

            monitor.setPresentationName(report.getPresentationName());

                if(monitor.getPresentationName().contains("EMAG")){


                      ((ClientServerObjectGroup) sim.getMonitorManager().getGroupsManager().getObject("EMAG")).getGroupsManager().groupObjects("EMAG", new NeoObjectVector(new Object[] {monitor}), true);


                }


        }}


    }



}

