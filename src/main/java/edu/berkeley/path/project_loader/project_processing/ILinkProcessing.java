package edu.berkeley.path.project_loader.project_processing;

import edu.berkeley.path.model_objects.network.Link;
import edu.berkeley.path.model_objects.scenario.FundamentalDiagramSet;

import java.util.List;

/**
 *
 */
public interface ILinkProcessing {

    // This method takes a List of MO-style Links and an id for a Fundamental Diagram Set construction object
    // ( which contains a list of Fundamental Diagram Profile construction objects) that can be used to find
    // the correct profile type for each link that lacks a profile.
    // returns a Model Objects-style FDSet that contains a list of MO-style FDProfile
    public FundamentalDiagramSet setFDProfiles(List<Link> links, long fdSetid);


}
