package com.teleport.loadoptimizer.service;

import com.teleport.loadoptimizer.model.request.OrderInput;
import com.teleport.loadoptimizer.model.request.TruckInput;

import java.util.List;

public record CacheRequest(TruckInput truck, List<OrderInput> sortedOrders) {}
