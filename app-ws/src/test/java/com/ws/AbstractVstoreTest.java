package com.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vs.service.repository.UserRepository;
import com.vs.service.repository.VerificationTokenRepository;
import com.vs.ws.AuthApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = AuthApplication.class)
public class AbstractVstoreTest {

    public static final String AUTH_API_BASE = "/api/auth";
    protected ObjectMapper om = new ObjectMapper();
    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected VerificationTokenRepository verificationTokenRepository;
    @Autowired
    protected UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        om.registerModule(new JavaTimeModule());
    }

    protected <REQ, RES> RES sendPost(String url,
                                      REQ request,
                                      Class<RES> response) throws Exception {
        return om.readValue(mockMvc.perform(post(url)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .content(om.writeValueAsBytes(request)))
                                    .andDo(print())
                                    .andExpect(status().isOk())
                                    .andReturn().getResponse().getContentAsString(),
                            response);
    }

    protected <RES> RES sendGet(String url,
                                Class<RES> returnType) throws Exception {
        String response = mockMvc.perform(get(url))
                .andDo(print())
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return isJSONValid(response) ? om.readValue(response, returnType) : (RES) response;

    }

    public boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }
}
