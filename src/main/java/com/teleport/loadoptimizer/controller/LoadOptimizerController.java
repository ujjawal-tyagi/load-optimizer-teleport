package com.teleport.loadoptimizer.controller;

import com.teleport.loadoptimizer.model.request.OptimizeRequest;
import com.teleport.loadoptimizer.model.response.OptimizeResponse;
import com.teleport.loadoptimizer.service.LoadOptimizerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/load-optimizer")
public class LoadOptimizerController {

    private final LoadOptimizerService optimizerService;

    public LoadOptimizerController(LoadOptimizerService optimizerService) {
        this.optimizerService = optimizerService;
    }

    @PostMapping("/optimize")
    public ResponseEntity<OptimizeResponse> optimize(
            @Valid @RequestBody OptimizeRequest request) {
        OptimizeResponse response = optimizerService.optimize(request);
        return ResponseEntity.ok(response);
    }
}
