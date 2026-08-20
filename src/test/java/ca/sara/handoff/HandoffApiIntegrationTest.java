package ca.sara.handoff;

import ca.sara.handoff.repository.HandoffEventRepository;
import ca.sara.handoff.repository.HandoffRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "handoff.seed.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:handoff-test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class HandoffApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HandoffEventRepository eventRepository;

    @Autowired
    private HandoffRepository handoffRepository;

    @BeforeEach
    void clearDatabase() {
        eventRepository.deleteAll();
        handoffRepository.deleteAll();
    }

    @Test
    void createsAValidatedHandoffAndRecordsItsHistory() throws Exception {
        String response = mockMvc.perform(post("/api/handoffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("pickup order needs a second check"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(response);
        long id = created.get("id").asLong();

        mockMvc.perform(get("/api/handoffs/{id}/history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventType").value("CREATED"));
    }

    @Test
    void movesThroughAllowedStatusesAndRejectsGoingBackwards() throws Exception {
        String response = mockMvc.perform(post("/api/handoffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(patch("/api/handoffs/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ACKNOWLEDGED","note":"opening shift checked the pickup shelf"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));

        mockMvc.perform(patch("/api/handoffs/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"DONE","note":"order was found and customer was called"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        mockMvc.perform(patch("/api/handoffs/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"NEW\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("cannot move a handoff from DONE to NEW"));

        mockMvc.perform(get("/api/handoffs/{id}/history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void returnsHelpfulFieldErrorsForInvalidRequests() throws Exception {
        mockMvc.perform(post("/api/handoffs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title":"",
                                  "details":"",
                                  "area":"sales floor",
                                  "shiftType":"OPENING",
                                  "priority":"HIGH",
                                  "owner":"sara"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("please check the highlighted fields"))
                .andExpect(jsonPath("$.fieldErrors.title").value("title is required"))
                .andExpect(jsonPath("$.fieldErrors.details").value("details are required"));
    }

    private String validRequest() {
        return """
                {
                  "title":"pickup order needs a second check",
                  "details":"the ready email was sent, but the bag is not in the usual location.",
                  "area":"online orders",
                  "shiftType":"MID",
                  "priority":"HIGH",
                  "owner":"sara"
                }
                """;
    }
}
