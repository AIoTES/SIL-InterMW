package eu.interiot.intermw.commons.responses;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.interiot.message.ID.EntityID;
import eu.interiot.message.Message;
import eu.interiot.message.managers.URI.URIManagerINTERMW;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListPlatformTypesRes extends BaseRes {
    private List<String> platformTypes = new ArrayList<>();

    public ListPlatformTypesRes() {
    }

    public ListPlatformTypesRes(Message message) {
        super(message);
        EntityID platformTypesEntityID = new EntityID(URIManagerINTERMW.PREFIX.intermw + "platform-types");
        Set<EntityID> entityIDs = message.getPayload().getEntityTypes(platformTypesEntityID);
        for (EntityID entityID : entityIDs) {
            platformTypes.add(entityID.toString());
        }
    }

    public static ListPlatformTypesRes fromJson(String json) throws IOException {
        return new ObjectMapper().readValue(json, ListPlatformTypesRes.class);
    }

    public List<String> getPlatformTypes() {
        return platformTypes;
    }
}
