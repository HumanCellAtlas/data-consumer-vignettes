package kmer;

import org.apache.commons.validator.UrlValidator;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import scala.Serializable;
import scala.Tuple2;

import java.io.IOException;
import java.util.ArrayList;


public class DataStoreClient implements Serializable {

    private String endpoint;

    public DataStoreClient(String _endpoint) {
        String[] schemes = {"https"};
        UrlValidator urlValidator = new UrlValidator(schemes);
        assert(urlValidator.isValid(_endpoint));
        assert(_endpoint.endsWith("/"));
        this.endpoint = _endpoint;
    }

    public ArrayList<Tuple2<String, String>> requestFileUrls(String uuid) throws JSONException, IOException, RetryCycleException, InterruptedException {
        JSONObject json = HttpUtil.readJsonFromUrl(this.endpoint + "bundles/" + uuid + "?replica=aws&presignedurls=true");
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