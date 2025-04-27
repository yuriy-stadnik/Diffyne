package com.syv.data.Diffyne.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class JsonPathExtractor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Object extract(String json, String jsonPath) throws Exception {
        JsonNode rootNode = objectMapper.readTree(json);
        return extractFromNode(rootNode, jsonPath);
    }

    public Object extract(Map<String, Object> data, String jsonPath) throws Exception {
        JsonNode rootNode = objectMapper.valueToTree(data);
        return extractFromNode(rootNode, jsonPath);
    }

    private Object extractFromNode(JsonNode node, String jsonPath) {
        if (jsonPath == null || jsonPath.isEmpty() || "$".equals(jsonPath)) {
            return nodeToObject(node);
        }

        String[] pathParts = jsonPath.split("\\.");
        JsonNode current = node;

        for (String part : pathParts) {
            if (part.equals("$")) continue;

            if (part.contains("[") && part.endsWith("]")) {
                // Array access
                String arrayName = part.substring(0, part.indexOf("["));
                int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.indexOf("]")));

                current = current.get(arrayName).get(index);
            } else {
                // Simple property access
                current = current.get(part);
            }

            if (current == null) {
                return null;
            }
        }

        return nodeToObject(current);
    }

    private Object nodeToObject(JsonNode node) {
        if (node.isArray()) {
            List<Object> result = new ArrayList<>();
            Iterator<JsonNode> elements = node.elements();
            while (elements.hasNext()) {
                result.add(nodeToObject(elements.next()));
            }
            return result;
        } else if (node.isObject()) {
            try {
                return objectMapper.treeToValue(node, Map.class);
            } catch (Exception e) {
                throw new RuntimeException("Error converting JsonNode to Map", e);
            }
        } else if (node.isTextual()) {
            return node.textValue();
        } else if (node.isNumber()) {
            return node.numberValue();
        } else if (node.isBoolean()) {
            return node.booleanValue();
        } else {
            return null;
        }
    }
}

