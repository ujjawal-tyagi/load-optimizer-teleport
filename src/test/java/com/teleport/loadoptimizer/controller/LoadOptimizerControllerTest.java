package com.teleport.loadoptimizer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoadOptimizerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String ENDPOINT = "/api/v1/load-optimizer/optimize";

    @Test
    void shouldReturn200WithOptimalSelectionFromPdfExample() throws Exception {
        String body = """
            {
              "truck": {
                "id": "truck-123",
                "max_weight_lbs": 44000,
                "max_volume_cuft": 3000
              },
              "orders": [
                {
                  "id": "ord-001",
                  "payout_cents": 250000,
                  "weight_lbs": 18000,
                  "volume_cuft": 1200,
                  "origin": "Los Angeles, CA",
                  "destination": "Dallas, TX",
                  "pickup_date": "2025-12-05",
                  "delivery_date": "2025-12-09",
                  "is_hazmat": false
                },
                {
                  "id": "ord-002",
                  "payout_cents": 180000,
                  "weight_lbs": 12000,
                  "volume_cuft": 900,
                  "origin": "Los Angeles, CA",
                  "destination": "Dallas, TX",
                  "pickup_date": "2025-12-04",
                  "delivery_date": "2025-12-10",
                  "is_hazmat": false
                },
                {
                  "id": "ord-003",
                  "payout_cents": 320000,
                  "weight_lbs": 30000,
                  "volume_cuft": 1800,
                  "origin": "Los Angeles, CA",
                  "destination": "Dallas, TX",
                  "pickup_date": "2025-12-06",
                  "delivery_date": "2025-12-08",
                  "is_hazmat": true
                }
              ]
            }
            """;

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truck_id").value("truck-123"))
                .andExpect(jsonPath("$.total_payout_cents").value(430000))
                .andExpect(jsonPath("$.total_weight_lbs").value(30000))
                .andExpect(jsonPath("$.total_volume_cuft").value(2100))
                .andExpect(jsonPath("$.selected_order_ids").isArray())
                .andExpect(jsonPath("$.selected_order_ids.length()").value(2))
                .andExpect(jsonPath("$.algorithm_used").isString())
                .andExpect(jsonPath("$.pareto_solutions").isArray());
    }

    @Test
    void shouldReturn200WithConfigurableWeightsPreferUtilization() throws Exception {
        String body = """
            {
              "truck": { "id": "truck-1", "max_weight_lbs": 44000, "max_volume_cuft": 3000 },
              "orders": [
                {
                  "id": "ord-big", "payout_cents": 500000, "weight_lbs": 40000, "volume_cuft": 2800,
                  "origin": "LA", "destination": "Dallas",
                  "pickup_date": "2025-12-01", "delivery_date": "2025-12-05", "is_hazmat": false
                },
                {
                  "id": "ord-small-a", "payout_cents": 100000, "weight_lbs": 10000, "volume_cuft": 800,
                  "origin": "LA", "destination": "Dallas",
                  "pickup_date": "2025-12-01", "delivery_date": "2025-12-05", "is_hazmat": false
                },
                {
                  "id": "ord-small-b", "payout_cents": 100000, "weight_lbs": 10000, "volume_cuft": 800,
                  "origin": "LA", "destination": "Dallas",
                  "pickup_date": "2025-12-01", "delivery_date": "2025-12-05", "is_hazmat": false
                }
              ],
              "preferences": { "revenue_weight": 0.0, "utilization_weight": 1.0 }
            }
            """;

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.truck_id").value("truck-1"))
                .andExpect(jsonPath("$.pareto_solutions").isArray());
    }

    @Test
    void shouldReturn400WhenOrdersListIsEmpty() throws Exception {
        String body = """
            {
              "truck": { "id": "truck-1", "max_weight_lbs": 44000, "max_volume_cuft": 3000 },
              "orders": []
            }
            """;
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTruckIsMissing() throws Exception {
        String body = """
            {
              "orders": [
                {
                  "id": "ord-001", "payout_cents": 100, "weight_lbs": 100, "volume_cuft": 10,
                  "origin": "A", "destination": "B",
                  "pickup_date": "2025-12-01", "delivery_date": "2025-12-05", "is_hazmat": false
                }
              ]
            }
            """;
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPickupDateIsAfterDeliveryDate() throws Exception {
        String body = """
            {
              "truck": { "id": "truck-1", "max_weight_lbs": 44000, "max_volume_cuft": 3000 },
              "orders": [
                {
                  "id": "ord-001", "payout_cents": 100000, "weight_lbs": 100, "volume_cuft": 10,
                  "origin": "A", "destination": "B",
                  "pickup_date": "2025-12-10",
                  "delivery_date": "2025-12-01",
                  "is_hazmat": false
                }
              ]
            }
            """;
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenNothingFitsTheTruck() throws Exception {
        String body = """
            {
              "truck": { "id": "tiny-truck", "max_weight_lbs": 1000, "max_volume_cuft": 100 },
              "orders": [
                {
                  "id": "ord-001", "payout_cents": 100000, "weight_lbs": 99999, "volume_cuft": 9999,
                  "origin": "A", "destination": "B",
                  "pickup_date": "2025-12-01", "delivery_date": "2025-12-05", "is_hazmat": false
                }
              ]
            }
            """;
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenDuplicateOrderIds() throws Exception {
        String body = """
            {
              "truck": { "id": "truck-1", "max_weight_lbs": 44000, "max_volume_cuft": 3000 },
              "orders": [
                {
                  "id": "SAME-ID", "payout_cents": 100000, "weight_lbs": 100, "volume_cuft": 10,
                  "origin": "A", "destination": "B",
                  "pickup_date": "2025-12-01", "delivery_date": "2025-12-05", "is_hazmat": false
                },
                {
                  "id": "SAME-ID", "payout_cents": 200000, "weight_lbs": 200, "volume_cuft": 20,
                  "origin": "A", "destination": "B",
                  "pickup_date": "2025-12-01", "delivery_date": "2025-12-05", "is_hazmat": false
                }
              ]
            }
            """;
        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn413WhenMoreThan22OrdersAreSubmitted() throws Exception {
        StringBuilder orders = new StringBuilder("[");
        for (int i = 1; i <= 23; i++) {
            if (i > 1) orders.append(",");
            orders.append(String.format("""
                {
                  "id": "ord-%03d", "payout_cents": 100000, "weight_lbs": 100, "volume_cuft": 10,
                  "origin": "A", "destination": "B",
                  "pickup_date": "2025-12-01", "delivery_date": "2025-12-05", "is_hazmat": false
                }
                """, i));
        }
        orders.append("]");

        String body = """
            {
              "truck": { "id": "truck-1", "max_weight_lbs": 44000, "max_volume_cuft": 3000 },
              "orders": """ + orders + """
            }
            """;

        mockMvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isPayloadTooLarge());
    }
}
