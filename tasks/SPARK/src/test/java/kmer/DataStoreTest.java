package kmer;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;


public class DataStoreTest {

    @Test
    public void testParseBundleManifest() throws InterruptedException, JSONException {
        JSONObject json = new JSONObject(
            "{\n" +
            "  \"bundle\": {\n" +
            "    \"creator_uid\": 8008,\n" +
            "    \"files\": [\n" +
            "      {\n" +
            "        \"content-type\": \"application/gzip; dcp-type=data\",\n" +
            "        \"crc32c\": \"4ef74578\",\n" +
            "        \"indexed\": false,\n" +
            "        \"name\": \"R1.fastq.gz\",\n" +
            "        \"s3_etag\": \"c7bbee4c46bbf29432862e05830c8f39\",\n" +
            "        \"sha1\": \"17f8b4be0cc6e8281a402bb365b1283b458906a3\",\n" +
            "        \"sha256\": \"fe6d4fdfea2ff1df97500dcfe7085ac3abfb760026bff75a34c20fb97a4b2b29\",\n" +
            "        \"size\": 125191,\n" +
            "        \"url\": \"https://presignedurl1.com\",\n" +
            "        \"uuid\": \"d4ebb0f1-4c3e-4fd6-8857-dd2cd49d4955\",\n" +
            "        \"version\": \"2018-07-07T213824.573052Z\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"content-type\": \"application/gzip; dcp-type=data\",\n" +
            "        \"crc32c\": \"69987b3e\",\n" +
            "        \"indexed\": false,\n" +
            "        \"name\": \"R2.fastq.gz\",\n" +
            "        \"s3_etag\": \"a3a9f23d07cfc5e40a4c3a8adf3903ae\",\n" +
            "        \"sha1\": \"f166b6952e30a41e1409e7fb0cb0fb1ad93f3f21\",\n" +
            "        \"sha256\": \"c305bee37b3c3735585e11306272b6ab085f04cd22ea8703957b4503488cfeba\",\n" +
            "        \"size\": 130024,\n" +
            "        \"url\": \"https://presignedurl2.com\",\n" +
            "        \"uuid\": \"43cb0075-53b9-4eed-8ddf-73bfbc8768fe\",\n" +
            "        \"version\": \"2018-07-07T213825.026271Z\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"uuid\": \"0b61834c-952a-4a5e-ad29-9a98aab568f6\",\n" +
            "    \"version\": \"2018-07-07T220454.237435Z\"\n" +
            "  }\n" +
            "}\n"
        );
        ArrayList<Tuple2<String,String>> results = DataStore.parseBundleManifest(json);
        ArrayList<Tuple2<String,String>> expected = new ArrayList<>(Arrays.asList(
                new Tuple2<String,String>("d4ebb0f1-4c3e-4fd6-8857-dd2cd49d4955", "https://presignedurl1.com"),
                new Tuple2<String,String>("43cb0075-53b9-4eed-8ddf-73bfbc8768fe", "https://presignedurl2.com")
        ));
        assertTrue(results.equals(expected));
    }
}

