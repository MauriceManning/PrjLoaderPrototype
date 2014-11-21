package edu.berkeley.path.project_loader;

import core.oraDatabase;
import edu.berkeley.path.model_database_access.scenario.FundamentalDiagramSetWriter;
import edu.berkeley.path.model_objects.network.Link;
import edu.berkeley.path.model_objects.scenario.Scenario;

import edu.berkeley.path.project_loader.shared.MDATestConfiguration;
import edu.berkeley.path.scenario.dao.IFundamentalDiagramSetDao;
import edu.berkeley.path.scenario.model.*;
import edu.berkeley.path.scenario.model.impl.*;
import edu.berkeley.path.scenario.service.IFundamentalDiagramManager;
import edu.berkeley.path.scenario.service.impl.FundamentalDiagramManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
@Configuration
@ComponentScan({"edu.berkeley.path"})
public class ConnectLinkToFDProfileTest {

    private static ProjectManager projectManager;
    private IFundamentalDiagramManager fundamentalDiagramManager;

    private static IFundamentalDiagramSet fundamentalDiagramSet;
    private static IFundamentalDiagramProfile fundamentalDiagramProfile, fundamentalDiagramProfile2;

    private static Connection connection;
    private static FundamentalDiagramSetWriter fdSetWriter;

    private long fundamentalDiagramSetId;
    private static List<Link> linkList;

    private ClassPathXmlApplicationContext context = null;
    private ClassPathXmlApplicationContext ormContext = null;
    private Logger logger = null;

    public static java.sql.Timestamp logtimestamp;

    // Constants for FD test values
    public static final long LINK_ID = 5L;
    public static final double START_TIME = 456.0;

    // Link Constants for test values
    public static String LINK_NAME = "LINK 1";
    public static double LANES = 2.0D;
    public static double LANE_OFFSET = 0.0d;
    public static double SPEED_LIMIT = 100.0;
    public static long LENGTH = 100l;

    private static final String NAME = "fundamental diagram set name";
    private static final String DESCRIPTION = "fundamental diagram set description";
    public static final long PROJECT_ID = 5;

    @Before
    public void setUp() throws Exception {
        //MDATestConfiguration.dbSetup();
        connection = oraDatabase.doConnect();

        this.context = new ClassPathXmlApplicationContext("springContextProjectLoader.xml");
        this.ormContext = new ClassPathXmlApplicationContext( "classpath*:**/springContextModelORM.xml");
        fundamentalDiagramManager = (FundamentalDiagramManager) this.ormContext.getBean("fundamentalDiagramManager");

        this.logger = LogManager.getLogger(IFundamentalDiagramSetDao.class.getName());

        linkList = new ArrayList<Link>();
        logtimestamp = new java.sql.Timestamp( System.currentTimeMillis() );

    }


    @Test
    public void testRead()  {
        try {

            //Scenario scenario = projectManager.getScenario(SCENARIO_ID);
           projectManager = this.context.getBean(ProjectManager.class);

            createInputFDSet();
            createLinks();

            edu.berkeley.path.model_objects.scenario.FundamentalDiagramSet fdsMO = projectManager.setLinkProfiles(linkList, fundamentalDiagramSetId);

            fdSetWriter = new FundamentalDiagramSetWriter(connection);

            long fdSetId = fdSetWriter.insert(fdsMO);
            assertTrue(fdSetId != 0);

        } catch (Exception ex) {
            logger.info("ConnectLinkToFDProfileTest testRead  Exception ex:" + ex.getMessage());
            logger.info("ConnectLinkToFDProfileTest testRead  Exception2 ex:" + ex);

            // assert fails if exception is thrown
            fail();
        }
    }


    protected void createInputFDSet() {

        // create a FDSet with two profiles, add a link type to one of them.

        fundamentalDiagramProfile = new FundamentalDiagramProfile();
        fundamentalDiagramProfile.setDt(1.0D);
        fundamentalDiagramProfile.setLinkId(LINK_ID);
        fundamentalDiagramProfile.setStartTime(START_TIME);
        fundamentalDiagramProfile.setModStamp(logtimestamp);
        fundamentalDiagramProfile.setSpeedLimit(SPEED_LIMIT);
        fundamentalDiagramProfile.setNumLanes(LANES);


        fundamentalDiagramProfile2 = new FundamentalDiagramProfile();
        fundamentalDiagramProfile2.setDt(1.0D);
        fundamentalDiagramProfile2.setLinkId(LINK_ID);
        fundamentalDiagramProfile2.setStartTime(START_TIME);
        fundamentalDiagramProfile2.setModStamp(logtimestamp);


        // need to resolve: model-orm LinkType used in model-orm FundamentalDiagramProfile and the model-objects/jaxb LinkType
        // used in the model-objects Link. ok for now as we just compare that the id attribute matches in this prototype.
        edu.berkeley.path.scenario.model.ILinkType linkType = new edu.berkeley.path.scenario.model.impl.LinkType();
        linkType.setName("targetLink");
        fundamentalDiagramProfile2.setLinkType(linkType);
        linkType.setFundamentalDiagramProfile(fundamentalDiagramProfile2);


        fundamentalDiagramSet = new FundamentalDiagramSet();
        fundamentalDiagramSet.setDescription(DESCRIPTION);
        fundamentalDiagramSet.setProjectId(PROJECT_ID);
        fundamentalDiagramSet.setName(NAME);
        fundamentalDiagramSet.setModStamp(logtimestamp);

        List<IFundamentalDiagramProfile> fdpList = new ArrayList<IFundamentalDiagramProfile>();
        fdpList.add(fundamentalDiagramProfile);
        fdpList.add(fundamentalDiagramProfile2);
        fundamentalDiagramSet.setFundamentalDiagramProfiles(fdpList);
        fundamentalDiagramProfile.setFundamentalDiagramSet(fundamentalDiagramSet);
        fundamentalDiagramProfile2.setFundamentalDiagramSet(fundamentalDiagramSet);

        fundamentalDiagramSetId = fundamentalDiagramManager.addFundamentalDiagramSet(fundamentalDiagramSet);
        logger.info("createInputFDSet fundamentalDiagramSet id : " + fundamentalDiagramSetId );

    }


    protected void createLinks() {

        //create two links to be match with profiles.

        // Create Link and add it to network model object
        Link link1 = new Link();
        link1.setInSync(true);
        link1.setLinkName(LINK_NAME);
        link1.setLaneOffset(LANE_OFFSET);
        link1.setLength(LENGTH);

        edu.berkeley.path.model_objects.jaxb.LinkType jaxbLinkType = new edu.berkeley.path.model_objects.jaxb.LinkType();
        jaxbLinkType.setId(1L);
        jaxbLinkType.setName("targetLink");
        link1.setLinkType(jaxbLinkType);


        // Create Link and add it to network model object
        Link link2 = new Link();
        link2.setInSync(true);
        link2.setLinkName(LINK_NAME);
        link2.setLanes(LANES);
        link2.setLaneOffset(LANE_OFFSET);
        link2.setSpeedLimit(SPEED_LIMIT);
        link2.setLength(LENGTH);

        linkList.add(link1);
        linkList.add(link2);

    }


}
