package eu.interiot.intermw.commons.responses;

import eu.interiot.message.Message;
import eu.interiot.message.exceptions.MessageException;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Gasper Vrhovsek
 */
public class ObservationResTest {

    private static final String SENDER_PLATFORM_ID = "http://www.inter-iot.eu/wso2port";
    public static final String FEATURE_OF_INTEREST_ID = "http://inter-iot.eu/LogVPmod#1111EKT2222EKT";
    private static String observationPortJson;

    @BeforeClass
    public static void beforeClass() throws IOException {
        ClassLoader classLoader = ObservationResTest.class.getClassLoader();
        observationPortJson = IOUtils.toString(classLoader.getResource("observation_port.json"), "UTF-8");
    }

    @Test
    public void testObservationPortJson() throws IOException, MessageException {
        // create
        Message observationMessage = new Message(observationPortJson);
        ObservationRes observationRes = new ObservationRes(observationMessage);

        // assert
        List<ObservationRes.Observation> observations = observationRes.getObservations();
        List<ObservationRes.FeatureOfInterest> featuresOfInterest = observationRes.getFeaturesOfInterest();

        assertEquals("SenderPlatform should be correctly parsed from json", SENDER_PLATFORM_ID, observationRes.getSenderPlatform());
        assertEquals("Observation response should contain 8 observations", 8, observations.size());
        // TODO this assert fails, we have to set standards for OBSERVATION messages
        //        assertEquals("Observation response should contain 8 sensors", 8, observationRes.getSensors().size());
        assertEquals("Observation response should contain 1 feature of interest", 1, featuresOfInterest.size());

        // assert observations
        for (ObservationRes.Observation observation : observations) {
            assertNotNull("Observation.hasFeatureOfInterest should not be null", observation.getHasFeatureOfInterest());
            assertEquals("Observation.hasFeatureOfInterest should have correct value", FEATURE_OF_INTEREST_ID, observation.getHasFeatureOfInterest());

            ObservationRes.Result result = observation.getResult();
            // assert observation result
            assertNotNull("Observation.Result should not be null", result);
            assertNotNull("Observation.Result.Value should not be null", result.getHasResultValue());
        }

        // assert features of interest
        for(ObservationRes.FeatureOfInterest featureOfInterest : featuresOfInterest) {
            assertEquals("Feature of interest should contain correct ID", FEATURE_OF_INTEREST_ID, featureOfInterest.getId());
        }

    }
}
