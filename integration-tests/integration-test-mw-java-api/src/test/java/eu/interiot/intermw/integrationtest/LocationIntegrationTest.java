package eu.interiot.intermw.integrationtest;

import eu.interiot.intermw.api.InterMwApiImpl;
import eu.interiot.intermw.api.exception.BadRequestException;
import eu.interiot.intermw.api.exception.ConflictException;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.Location;
import eu.interiot.intermw.commons.model.Platform;
import eu.interiot.intermw.services.registry.ParliamentRegistryExperimental;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Gasper Vrhovsek
 */
public class LocationIntegrationTest {

    private static final String PLATFORM_ID = "http://test.inter-iot.eu/test-platform1";
    private static final String LOCATION = "http://test.inter-iot.eu/TestLocation";

    @Test(expected = ConflictException.class)
    public void savingDuplicateLocationWithPlatform() throws MiddlewareException, ConflictException, BadRequestException {
        // mock api and parliament
        InterMwApiImpl intermwApiMock =
                mock(InterMwApiImpl.class);
        ParliamentRegistryExperimental parliamentRegistryMock =
                mock(ParliamentRegistryExperimental.class);

        // pojos for test
        Location parliamentLocationMock = new Location(LOCATION);
        parliamentLocationMock.setDescription("location description");
        parliamentLocationMock.setLatitude(10d);
        parliamentLocationMock.setLongitude(20d);

        Platform platform = new Platform();
        platform.setPlatformId(PLATFORM_ID);

        Location exceptionTriggerLocation = new Location(LOCATION);
        exceptionTriggerLocation.setDescription("alternate description");
        exceptionTriggerLocation.setLatitude(20d);
        exceptionTriggerLocation.setLongitude(10d);

        // mock method returns
        when(parliamentRegistryMock.getLocation(LOCATION)).thenReturn(parliamentLocationMock);
        when(parliamentRegistryMock.getPlatformById(PLATFORM_ID)).thenReturn(null);
        when(intermwApiMock.getRegistry()).thenReturn(parliamentRegistryMock);
        when(intermwApiMock.registerPlatform(platform, exceptionTriggerLocation)).thenCallRealMethod();

        intermwApiMock.registerPlatform(platform, exceptionTriggerLocation);
    }

    @Test
    public void savingUniqueLocationWithPlatform() throws MiddlewareException, ConflictException, BadRequestException {
        String description = "description";

        // simulate DB
        final Map<String, Platform> platformMap = new HashMap<>();
        final Map<String, Location> locationMap = new HashMap<>();

        // mock api and parliament
        InterMwApiImpl intermwApiMock =
                mock(InterMwApiImpl.class);
        ParliamentRegistryExperimental parliamentRegistryMock =
                mock(ParliamentRegistryExperimental.class);

        // pojos for test
        Platform platform = new Platform();
        platform.setPlatformId(PLATFORM_ID);
        platform.setLocationId(LOCATION);

        platform.setLocation(new Location(LOCATION, description, 20d, 10d));

        Location location = new Location(LOCATION);
        location.setDescription(description);
        location.setLatitude(20d);
        location.setLongitude(10d);

        // mock method returns
        when(parliamentRegistryMock
                .getLocation(platform.getLocationId())).then(invocationOnMock -> locationMap.get(platform.getLocationId()));
        when(parliamentRegistryMock.getPlatformById(PLATFORM_ID)).then(invocationOnMock -> platformMap.get(PLATFORM_ID));

        when(intermwApiMock.getRegistry()).thenReturn(parliamentRegistryMock);

        when(intermwApiMock.registerPlatform(platform, location)).then(invocationOnMock -> {
            platformMap.put(platform.getPlatformId(), platform);
            locationMap.put(location.getLocationId(), location);
            return "";
        }).thenCallRealMethod();
        when(intermwApiMock.getPlatform(PLATFORM_ID)).thenCallRealMethod();

        intermwApiMock.registerPlatform(platform, location);

        Platform platform1 = intermwApiMock.getPlatform(PLATFORM_ID);

        assertEquals(LOCATION, platform1.getLocationId());
        assertEquals(description, platform1.getLocation().getDescription());
        assertEquals(new Double(20d), platform1.getLocation().getLatitude());
        assertEquals(new Double(10d), platform1.getLocation().getLongitude());
    }
}
