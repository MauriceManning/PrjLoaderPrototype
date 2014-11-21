package edu.berkeley.path.project_loader.project_processing.impl;

import edu.berkeley.path.model_objects.network.Link;
import edu.berkeley.path.model_objects.scenario.FundamentalDiagramSet;
import edu.berkeley.path.project_loader.project_processing.ILinkProcessing;
import edu.berkeley.path.scenario.model.IFundamentalDiagramProfile;
import edu.berkeley.path.scenario.service.IFundamentalDiagramManager;
import edu.berkeley.path.scenario.model.IFundamentalDiagramSet;

import com.google.common.collect.Multimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;
import com.google.common.collect.HashBasedTable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 *
 */
public class LinkProcessing implements ILinkProcessing {

    @Autowired
    private IFundamentalDiagramManager fundamentalDiagramManager;
    public void setFundamentalDiagramManager(IFundamentalDiagramManager manager) { this.fundamentalDiagramManager = manager; }

    public FundamentalDiagramSet setFDProfiles(List<Link> linksList, long fdSetid) {

        FundamentalDiagramSet retvalFDS = new FundamentalDiagramSet();



        // this is a simple matching algorithm that walks each link in the list and attempts to find a matching profile
        // First it check if there is a profile with matching speedlimit and number of lanes, if not then it checks
        // if there is a profile with a match link type. It is assumed that one of these two cases will match for this
        // prototype.
        // it is also assumed that there will only be one profile that matches in the above cases.


        IFundamentalDiagramSet fdSetCO = fundamentalDiagramManager.getFundamentalDiagramSet(fdSetid);

        //copy over metadata
        retvalFDS.setName(fdSetCO.getName());
        retvalFDS.setProjectId(fdSetCO.getProjectId());
        retvalFDS.setDescription(fdSetCO.getDescription());

        //create collections which will facilitate matching process
        Map linkToProfileMap = new HashMap<Link, IFundamentalDiagramProfile>();
        //create a guava Table with speedLimit and numLanes as keys, fdp as value
        Table<Double, Double, IFundamentalDiagramProfile > numLanesSpeedLimitTable = HashBasedTable.create();
        // create a map for the link type to the profile, assume unique for this ptototype
        Map linkTypeToProfileMap = new HashMap<String, IFundamentalDiagramProfile>();


        // loop thru the profiles and create a maps
        List<IFundamentalDiagramProfile> fdpList = fdSetCO.getFundamentalDiagramProfiles();
        for (IFundamentalDiagramProfile fdp : fdpList) {

            // if profile contains a speed limit and number of lanes, add to map
            if (fdp.getSpeedLimit() != null && fdp.getNumLanes() != null) {
                numLanesSpeedLimitTable.put(fdp.getSpeedLimit(), fdp.getNumLanes(), fdp);
            }
            // if profile contains a link type, add to map
            if (fdp.getLinkType() != null) {
                String ltName = fdp.getLinkType().getName();
                linkTypeToProfileMap.put(ltName , fdp);
            }

        }


        // loop thru the set of links to find links w/o a matching profile
        for (Link link : linksList) {


            // check if there is a speedLimit / numLanes match with one of the profiles
            if ( numLanesSpeedLimitTable.contains(link.getSpeedLimit(), link.getLanes()) ) {
                IFundamentalDiagramProfile ifdp =  numLanesSpeedLimitTable.get(link.getSpeedLimit(), link.getLanes());
                linkToProfileMap.put(link, ifdp);
            }

            // check if there is a match with the link type
            if (link.getLinkType() != null && linkTypeToProfileMap.containsKey(link.getLinkType().getName())) {
                IFundamentalDiagramProfile ifdp = (IFundamentalDiagramProfile) linkTypeToProfileMap.get(link.getLinkType().getName());
                        linkToProfileMap.put(link, ifdp );
            }

            // log error here, not able to match a profile to this link

        }


        //create the MO-style FundamentalDiagramProfile for each link and connect, place into a MO-style FundamentalDiagramSet
        Iterator iterator = linkToProfileMap.keySet().iterator();
        while( iterator.hasNext() ) {
            Link link   = (Link) iterator.next();
            IFundamentalDiagramProfile profile = (IFundamentalDiagramProfile) linkToProfileMap.get(link);
            edu.berkeley.path.model_objects.scenario.FundamentalDiagramProfile modelObjectsProfile = createModelObjectsProfile(profile);
            modelObjectsProfile.setLinkId(link.getId());
            retvalFDS.getListOfFundamentalDiagramProfiles().add(modelObjectsProfile);

        }

        return  retvalFDS;


    }

    protected edu.berkeley.path.model_objects.scenario.FundamentalDiagramProfile createModelObjectsProfile(IFundamentalDiagramProfile profile) {

        //create a Model Objects style FundamentalDiagramProfile, then copy over attribute from new style FundamentalDiagramProfile
        edu.berkeley.path.model_objects.scenario.FundamentalDiagramProfile newProfile = new edu.berkeley.path.model_objects.scenario.FundamentalDiagramProfile();
        newProfile.setAggRunId( profile.getAggRunId() );
        newProfile.setDt( profile.getDt() );
        newProfile.setSensorId( profile.getSensorId() );

        // need to *deep copy* here: also create MO-style calibarationAlgoithmType, FundamentalDiagramType subobjects

        return newProfile;

    }

}
