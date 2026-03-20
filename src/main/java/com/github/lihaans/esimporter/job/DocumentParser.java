package com.github.lihaans.esimporter.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.lihaans.esimporter.config.JobConfig;
import com.github.lihaans.esimporter.model.DocumentRecord;
import com.github.lihaans.esimporter.util.HashUtils;

public class DocumentParser {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final JobConfig config;

    public DocumentParser(JobConfig config) {
        this.config = config;
    }

    public DocumentRecord parse(String filePath, long lineNo, String line) throws Exception {
        JsonNode node = MAPPER.readTree(line);
        String id = extractId(node, line);
        String routing = null;
        if (config.getRoutingField() != null && !config.getRoutingField().trim().isEmpty()) {
            JsonNode routingNode = node.get(config.getRoutingField());
            if (routingNode != null && !routingNode.isNull()) {
                routing = routingNode.asText();
            }
        }
        return new DocumentRecord(filePath, lineNo, config.getIndexName(), id, routing, line);
    }

    private String extractId(JsonNode node, String raw) {
        if (config.getIdField() != null && !config.getIdField().trim().isEmpty()) {
            JsonNode idNode = node.get(config.getIdField());
            if (idNode != null && !idNode.isNull()) {
                return idNode.asText();
            }
        }
        return HashUtils.md5Hex(raw);
    }
}
