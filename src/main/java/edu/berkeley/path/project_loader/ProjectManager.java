package edu.berkeley.path.project_loader;

import java.util.List;

import core.oraDatabase;
import edu.berkeley.path.model_objects.network.Link;
import edu.berkeley.path.model_objects.network.Network;
import edu.berkeley.path.model_objects.scenario.FundamentalDiagramSet;
import edu.berkeley.path.model_objects.scenario.Scenario;
import edu.berkeley.path.project_loader.project_processing.ILinkProcessing;
import edu.berkeley.path.model_database_access.scenario.ScenarioReader;
import org.springframework.beans.factory.annotation.Autowired;


/**
 *
 */
public class ProjectManager {

    @Autowired
    private static ILinkProcessing linkProcessing;
    public void setLinkProcessing(ILinkProcessing lp) {
        this.linkProcessing = lp;
    }


    private static ScenarioReader scenarioReader;

    public Scenario getScenario(long id) {

        // load the scenario
        scenarioReader= new ScenarioReader(oraDatabase.doConnect());

        try {
            Scenario scenario = scenarioReader.read(id);

            // retrieve the network and validate - multi steps here eventually

            // validate link / profile connections, augment as required.
            List<Network> networkList = scenario.getListOfNetworks();

            for (Network n : networkList) {
                List<Link> linkList = n.getListOfLinks();
                // the FDSet construction object to be used for setting profiles would
                // be identified in the Project structure of this scenario
                //setLinkProfiles(linkList, );

            }

        } catch (Exception e) {
            e.printStackTrace();
            // assert fails if exception is thrown
        }
        return null;
    }


    public FundamentalDiagramSet setLinkProfiles(List<Link> links, long fdSetid) {

        return linkProcessing.setFDProfiles(links, fdSetid);
    }


}
