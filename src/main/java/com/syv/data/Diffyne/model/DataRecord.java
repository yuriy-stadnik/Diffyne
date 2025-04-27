package com.syv.data.Diffyne.model;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class DataRecord {
    private UUID id;
    private Map<String, Object> data;
    private String primaryKeyValue;
}
