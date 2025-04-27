package com.syv.data.Diffyne.dto;


import lombok.Data;

import java.util.Map;

@Data
public class RestApiSnapshotRequest {
    private String url;
    private String name;
    private String authToken;
    private Map<String, String> headers;
    private String recordsPath;
    private String primaryKeyField;
    private Map<String, Object> additionalParams;
}
