package com.insightlens.core.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrchestrationService {
    
    public String performOrchestrationTask(String input) {
        log.info("Performing orchestration task with input: {}", input);
        return "Processed: " + input;
    }
} 