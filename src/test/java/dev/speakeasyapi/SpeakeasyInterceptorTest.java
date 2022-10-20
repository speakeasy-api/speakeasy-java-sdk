package dev.speakeasyapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import dev.speakeasyapi.sdk.SpeakeasyConfig;
import dev.speakeasyapi.sdk.SpeakeasyMiddlewareController;
import dev.speakeasyapi.springboot.SpeakeasyFilter;
import dev.speakeasyapi.springboot.SpeakeasyInterceptor;

@WebMvcTest(controllers = { SpeakeasyInterceptorTest.TestPathController.class,
        SpeakeasyInterceptorTest.RootPathController.class })
@TestInstance(Lifecycle.PER_CLASS)
class SpeakeasyInterceptorTest {
    @Autowired
    private MockMvc mockMvc;

    private TestSpeakeasyClient client;

    @BeforeEach
    public void setup() {
        client = new TestSpeakeasyClient();

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new SpeakeasyInterceptorTest.TestPathController(),
                        new SpeakeasyInterceptorTest.RootPathController())
                .addInterceptors(new SpeakeasyInterceptor(new SpeakeasyConfig(), client))
                .addFilter(new SpeakeasyFilter())
                .build();
    }

    @Configuration
    static class TestConfig {
    }

    @Test
    public void testPathHintRequestMapping() throws Exception {
        MockHttpServletRequestBuilder req = get("/test/requestMapping");

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());

        client.latch.await();

        assertEquals("/test/requestMapping", client.PathHint);
    }

    @Test
    public void testPathHintRequestMappingWithID() throws Exception {
        String id = "123";

        MockHttpServletRequestBuilder req = get("/test/requestMapping/" + id);

        ResultActions result = mockMvc.perform(req);

        result.andExpect(status().isOk()); // .andExpect(content().string(id)); TODO: figure out why response body is
        // empty

        client.latch.await();

        assertEquals("/test/requestMapping/{id}", client.PathHint);
    }

    @Test
    public void testPathHintGetMapping() throws Exception {
        MockHttpServletRequestBuilder req = get("/test/getMapping");

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());

        client.latch.await();

        assertEquals("/test/getMapping", client.PathHint);
    }

    @Test
    public void testPathHintPostMapping() throws Exception {
        MockHttpServletRequestBuilder req = post("/test/postMapping");

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());

        client.latch.await();

        assertEquals("/test/postMapping", client.PathHint);
    }

    @Test
    public void testPathHintPutMapping() throws Exception {
        MockHttpServletRequestBuilder req = put("/putMapping");

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());

        client.latch.await();

        assertEquals("/putMapping", client.PathHint);
    }

    @Test
    public void testPathHintDeleteMapping() throws Exception {
        MockHttpServletRequestBuilder req = delete("/deleteMapping");

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());

        client.latch.await();

        assertEquals("/deleteMapping", client.PathHint);
    }

    @Test
    public void testPathHintPatchMapping() throws Exception {
        MockHttpServletRequestBuilder req = patch("/patchMapping");

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());

        client.latch.await();

        assertEquals("/patchMapping", client.PathHint);
    }

    @Test
    public void testPathHintManual() throws Exception {
        MockHttpServletRequestBuilder req = get("/manualPathHint");

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());

        client.latch.await();

        assertEquals("/my/manual/path/hint", client.PathHint);
    }

    @Test
    public void testGetCustomerID() throws Exception {
        MockHttpServletRequestBuilder req = patch("/getCustomerID");

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());

        client.latch.await();

        assertEquals("a-customers-id", client.CustomerID);
    }

    @RestController
    @ComponentScan
    @RequestMapping("test")
    public static class TestPathController {
        @RequestMapping(value = "/requestMapping", method = RequestMethod.GET)
        public String requestMapping() {
            return "requestMapping";
        }

        @RequestMapping(value = "requestMapping/{id}", method = RequestMethod.GET)
        public String requestMappingWithID(@PathVariable("id") String id) {
            return id;
        }

        @GetMapping("/getMapping")
        public String getMapping() {
            return "getMapping";
        }

        @PostMapping("/postMapping")
        public String postMapping() {
            return "postMapping";
        }
    }

    @RestController
    @ComponentScan
    public static class RootPathController {
        @PutMapping("/putMapping")
        public String putMapping() {
            return "putMapping";
        }

        @DeleteMapping("/deleteMapping")
        public String deleteMapping() {
            return "deleteMapping";
        }

        @PatchMapping("/patchMapping")
        public String patchMapping() {
            return "patchMapping";
        }

        @GetMapping("/manualPathHint")
        public String manualPathHint(
                @RequestAttribute(SpeakeasyMiddlewareController.ControllerKey) SpeakeasyMiddlewareController controller) {

            controller.setPathHint("/my/manual/path/hint");

            return "manualPathHint";
        }

        @PatchMapping("/getCustomerID")
        public String getCustomerID(
                @RequestAttribute(SpeakeasyMiddlewareController.ControllerKey) SpeakeasyMiddlewareController controller) {

            controller.setCustomerID("a-customers-id");

            return "getCustomerID";
        }
    }
}
