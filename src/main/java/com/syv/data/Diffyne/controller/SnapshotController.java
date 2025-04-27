package com.syv.data.Diffyne.controller;


import com.syv.data.Diffyne.dto.SnapshotRequest;
import com.syv.data.Diffyne.model.*;
import com.syv.data.Diffyne.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/snapshots")
public class SnapshotController {
    @Autowired
    private SnapshotService snapshotService;

    @PostMapping
    public DataSnapshot createSnapshot(@RequestBody SnapshotRequest request) {
        return snapshotService.createSnapshot(request.getSourceType(),
                request.getSourceIdentifier(),
                request.getParams());
    }

    @GetMapping("/{id}")
    public DataSnapshot getSnapshot(@PathVariable UUID id) {
        return snapshotService.getSnapshot(id);
    }

    // Other endpoints
}

