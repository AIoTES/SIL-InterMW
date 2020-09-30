package eu.interiot.intermw.services.registry;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import eu.interiot.intermw.commons.exceptions.MiddlewareException;
import eu.interiot.intermw.commons.model.enums.IoTDeviceType;
import org.apache.jena.query.ParameterizedSparqlString;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gasper Vrhovsek
 */
// TODO check if this can be removed
public class QueryBuilder {
    public static class IoTDevice {
        public static String delete(eu.interiot.intermw.commons.model.IoTDevice deviceToDelete) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public static ParameterizedSparqlString get(eu.interiot.intermw.commons.model.IoTDevice deviceToGet) throws MiddlewareException {
            String templateName = "builder-device-get-template.rq";
            String template = getTemplate(templateName);

            StringBuilder filterStringBuilder = new StringBuilder();

            if (deviceToGet.getDeviceId() != null) {
                filterStringBuilder.append("FILTER(?deviceId in (<").append(deviceToGet.getDeviceId()).append(">))");
            }
            if (deviceToGet.getHostedBy() != null) {
                filterStringBuilder.append("FILTER(?hostedBy in (<").append(deviceToGet.getHostedBy()).append(">))");
            }
            if (deviceToGet.getLocation() != null) {
                filterStringBuilder.append("FILTER(?location in (\"").append(deviceToGet.getLocation()).append("\"))");
            }
            if (deviceToGet.getName() != null) {
                filterStringBuilder.append("FILTER(?name in (\"").append(deviceToGet.getName()).append("\"))");
            }
            if (deviceToGet.getMadeActuation() != null) {
                filterStringBuilder.append("FILTER(?madeActuation in (<").append(deviceToGet.getMadeActuation()).append(">))");
            }
            if (deviceToGet.getImplementsProcedure() != null) {
                filterStringBuilder.append("FILTER(?implementsProcedure in (<").append(deviceToGet.getImplementsProcedure()).append(">))");
            }
            if (deviceToGet.getDetects() != null) {
                filterStringBuilder.append("FILTER(?detects in (<").append(deviceToGet.getDetects()).append(">))");
            }
            if (deviceToGet.getMadeObservation() != null) {
                filterStringBuilder.append("FILTER(?madeObservation in (<").append(deviceToGet.getMadeObservation()).append(">))");
            }

            if (deviceToGet.getObserves() != null && !deviceToGet.getObserves().isEmpty()) {
                filterStringBuilder.append("FILTER(");
                for (String observes : deviceToGet.getObserves()) {
                    filterStringBuilder.append("?observes = (<").append(observes).append(">) || ");
                }
                filterStringBuilder.delete(filterStringBuilder.length() - 4, filterStringBuilder.length()).append(")");
            }

            if (deviceToGet.getForProperty() != null && !deviceToGet.getForProperty().isEmpty()) {
                filterStringBuilder.append("FILTER(");
                for (String forProperty : deviceToGet.getForProperty()) {
                    filterStringBuilder.append("?isForProperty = (<").append(forProperty).append(">) || ");
                }
                filterStringBuilder.delete(filterStringBuilder.length() - 4, filterStringBuilder.length()).append(")");
            }

            if (deviceToGet.getDeviceTypes() != null && !deviceToGet.getDeviceTypes().isEmpty()) {
                filterStringBuilder.append("FILTER(");
                for (IoTDeviceType type : deviceToGet.getDeviceTypes()) {
                    filterStringBuilder.append("?type = \"").append(type.getDeviceTypeUri()).append("\" || ");
                }
                filterStringBuilder.delete(filterStringBuilder.length() - 4, filterStringBuilder.length()).append(")");
            }

            if (deviceToGet.getHosts() != null && !deviceToGet.getHosts().isEmpty()) {
                filterStringBuilder.append("FILTER(?hosts in (");
                for (String hosts : deviceToGet.getHosts()) {
                    filterStringBuilder.append("<").append(hosts).append(">").append(",");
                }
                filterStringBuilder.append("))");
            }

            template = template.replace("{filter_container}", filterStringBuilder.toString());
            ParameterizedSparqlString pss = new ParameterizedSparqlString();
            pss.setCommandText(template);

            return pss;
        }

        public static ParameterizedSparqlString update(eu.interiot.intermw.commons.model.IoTDevice deviceToUpdate) throws MiddlewareException {
            validateForInsert(deviceToUpdate);

            String template = getTemplate("builder-device-update-template.rq");

            Map<String, String> iri = new HashMap<>();
            Map<String, String> literals = new HashMap<>();
            //

            StringBuilder templateStringBuilder = new StringBuilder();
            templateStringBuilder
                    .append("?deviceId mdw:name ?name;").append("\n")
                    .append("mdw:hostedBy ?hostedBy;").append("\n")
                    .append("mdw:location ?location;").append("\n");
            for (IoTDeviceType type : deviceToUpdate.getDeviceTypes()) {
                String typeId = "?type" + type.name();
                templateStringBuilder.append("mdw:type ").append(typeId).append(";").append("\n");
                literals.put(typeId, type.name());
            }

            iri.put("?deviceId", deviceToUpdate.getDeviceId());
            iri.put("?hostedBy", deviceToUpdate.getHostedBy());
            literals.put("?location", deviceToUpdate.getLocation());
            literals.put("?name", deviceToUpdate.getName());

            addOptionalAttributeSet("hosts", deviceToUpdate.getHosts(), iri, templateStringBuilder);
            addOptionalAttributeSet("isForProperty", deviceToUpdate.getForProperty(), iri, templateStringBuilder);
            addOptionalAttributeSet("observes", deviceToUpdate.getObserves(), iri, templateStringBuilder);

            addOptionalAttribute("location", deviceToUpdate.getLocation(), literals, templateStringBuilder);
            addOptionalAttribute("madeActuation", deviceToUpdate.getMadeActuation(), iri, templateStringBuilder);
            addOptionalAttribute("implementsProcedure", deviceToUpdate.getImplementsProcedure(), iri, templateStringBuilder);
            addOptionalAttribute("detects", deviceToUpdate.getDetects(), iri, templateStringBuilder);
            addOptionalAttribute("madeObservation", deviceToUpdate.getMadeObservation(), iri, templateStringBuilder);

            String templateString = template.replace("{content}", templateStringBuilder.toString());

            ParameterizedSparqlString pss = new ParameterizedSparqlString();
            pss.setCommandText(templateString);

            for (Map.Entry<String, String> entry : iri.entrySet()) {
                pss.setIri(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : literals.entrySet()) {
                pss.setLiteral(entry.getKey(), entry.getValue());
            }

            System.out.println(pss.toString());

            return pss;
        }

        public static ParameterizedSparqlString insert(eu.interiot.intermw.commons.model.IoTDevice deviceToInsert) throws MiddlewareException {
            // validation
            validateForInsert(deviceToInsert);

            String template = getTemplate("builder-device-add-template.rq");

            Map<String, String> iri = new HashMap<>();
            Map<String, String> literals = new HashMap<>();
            //

            StringBuilder templateStringBuilder = new StringBuilder();
            templateStringBuilder
                    .append("?deviceId mdw:name ?name;").append("\n")
                    .append("mdw:hostedBy ?hostedBy;").append("\n");
            for (IoTDeviceType type : deviceToInsert.getDeviceTypes()) {
                String typeId = "?type" + type.name();
                templateStringBuilder.append("mdw:type ").append(typeId).append(";").append("\n");
                literals.put(typeId, type.name());
            }

            iri.put("?deviceId", deviceToInsert.getDeviceId());
            iri.put("?hostedBy", deviceToInsert.getHostedBy());
            literals.put("?name", deviceToInsert.getName());

            addOptionalAttributeSet("hosts", deviceToInsert.getHosts(), iri, templateStringBuilder);
            addOptionalAttributeSet("isForProperty", deviceToInsert.getForProperty(), iri, templateStringBuilder);
            addOptionalAttributeSet("observes", deviceToInsert.getObserves(), iri, templateStringBuilder);

            addOptionalAttribute("location", deviceToInsert.getLocation(), literals, templateStringBuilder);
            addOptionalAttribute("madeActuation", deviceToInsert.getMadeActuation(), iri, templateStringBuilder);
            addOptionalAttribute("implementsProcedure", deviceToInsert.getImplementsProcedure(), iri, templateStringBuilder);
            addOptionalAttribute("detects", deviceToInsert.getDetects(), iri, templateStringBuilder);
            addOptionalAttribute("madeObservation", deviceToInsert.getMadeObservation(), iri, templateStringBuilder);

            String templateString = template.replace("{content}", templateStringBuilder.toString());

            ParameterizedSparqlString pss = new ParameterizedSparqlString();
            pss.setCommandText(templateString);

            for (Map.Entry<String, String> entry : iri.entrySet()) {
                pss.setIri(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, String> entry : literals.entrySet()) {
                pss.setLiteral(entry.getKey(), entry.getValue());
            }

            return pss;
        }

        private static void validateForInsert(eu.interiot.intermw.commons.model.IoTDevice deviceToInsert) {
            if (Strings.isNullOrEmpty(deviceToInsert.getDeviceId())) {
                throw new IllegalArgumentException("IoTDevice insert query can not be constructed without deviceId field.");
            }
            if (Strings.isNullOrEmpty(deviceToInsert.getName())) {
                throw new IllegalArgumentException("IoTDevice insert query can not be constructed without name field.");
            }
            if (Strings.isNullOrEmpty(deviceToInsert.getHostedBy())) {
                throw new IllegalArgumentException("IoTDevice insert query can not be constructed without hostedBy field.");
            }
        }

    }

    private static void addOptionalAttribute(String attribute, String attributeValue, Map<String, String> iri, StringBuilder templateStringBuilder) {
        if (!Strings.isNullOrEmpty(attributeValue)) {
            templateStringBuilder.append("mdw:").append(attribute).append(" ?").append(attribute).append(";\n");
            iri.put("?" + attribute, attributeValue);
        }
    }

    private static void addOptionalAttributeSet(String attribute, Collection<String> attributeValueCollection, Map<String, String> iri, StringBuilder templateStringBuilder) {
        if (attributeValueCollection != null) {
            int i = 0;
            for (String hosts : attributeValueCollection) {
                String attributeId = "?" + attribute + i;
                templateStringBuilder.append("mdw:").append(attribute).append(" ").append(attributeId).append(";").append("\n");
                iri.put(attributeId, hosts);
                i++;
            }
        }
    }

    private static String getTemplate(String templateName) throws MiddlewareException {
        URL url = Resources.getResource(templateName);
        String template;
        try {
            template = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new MiddlewareException("Failed to load SPARQL query.", e);
        }
        return template;
    }
}
