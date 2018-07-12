package kmer;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;


public class DataStore {

    public static ArrayList<Tuple2<String, String>> requestFileUrls(String uuid) throws JSONException, IOException, RetryCycleException, InterruptedException {
        JSONObject json = HttpUtil.readJsonFromUrl(
                "https://dss.data.humancellatlas.org/v1/bundles/" + uuid + "?replica=aws&presignedurls=true"
        );
        return parseBundleManifest(json);
    }

    public static ArrayList<Tuple2<String, String>> parseBundleManifest(JSONObject json) throws JSONException {
        ArrayList<Tuple2<String, String>> results = new ArrayList<>();
        for (int i = 0; i < ((JSONObject) json.get("bundle")).getJSONArray("files").length(); i++) {
            JSONObject o = ((JSONObject) json.get("bundle")).getJSONArray("files").getJSONObject(i);
            String fileUrl = (String) o.get("url");
            String fileUuid = (String) o.get("uuid");
            if ("application/gzip; dcp-type=data".equals((String) o.get("content-type"))) {
                results.add(new Tuple2<>(fileUuid, fileUrl));
            }
        }
        return results;
    }
}