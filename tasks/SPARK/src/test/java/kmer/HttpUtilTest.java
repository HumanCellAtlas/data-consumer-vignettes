package kmer;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import org.codehaus.jettison.json.JSONException;
import org.junit.Rule;

import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertEquals;


public class HttpUtilTest {

    int port = 8089;
    String basePath = "http://localhost:" + port;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(port));

    @Test
    public void readLocationFromUrlShouldHandleHttp301() throws InterruptedException, JSONException, IOException, RetryCycleException {
        stubFor(get(urlEqualTo("/endpoint1"))
                .willReturn(aResponse()
                        .withStatus(301)
                        .withHeader("Retry-After", "1")
                        .withHeader("location", basePath + "/endpoint2")
                        .withBody("{\"status\": \"Retry again in a little bit!\"}")));

        stubFor(get(urlEqualTo("/endpoint2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"status\": \"OK\"}")));

        JSONObject json = HttpUtil.readJsonFromUrl(basePath + "/endpoint1");
        assertEquals(json.get("status"), "OK");
    }
}
