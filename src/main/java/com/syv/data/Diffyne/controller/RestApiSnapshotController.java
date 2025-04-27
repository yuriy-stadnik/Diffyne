package com.syv.data.Diffyne.controller;

import com.syv.data.Diffyne.dto.RestApiSnapshotRequest;
import com.syv.data.Diffyne.model.DataSnapshot;
import com.syv.data.Diffyne.service.SnapshotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rest-snapshots")
public class RestApiSnapshotController {

    @Autowired
    private SnapshotService snapshotService;

    @PostMapping
    public DataSnapshot createRestApiSnapshot(@RequestBody RestApiSnapshotRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("url", request.getUrl());
        params.put("authToken", request.getAuthToken());
        params.put("headers", request.getHeaders());
        params.put("recordsPath", request.getRecordsPath());
        params.put("primaryKeyField", request.getPrimaryKeyField());

        if (request.getAdditionalParams() != null) {
            params.putAll(request.getAdditionalParams());
        }

        return snapshotService.createSnapshot("REST_API", request.getUrl(), params);
    }

    @GetMapping("/{id}")
    public DataSnapshot getSnapshot(@PathVariable UUID id) {
        return snapshotService.getSnapshot(id);
    }
}

