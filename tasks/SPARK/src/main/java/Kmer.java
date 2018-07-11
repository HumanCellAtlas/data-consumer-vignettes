
// STEP-0: import required classes and interfaces

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import scala.Tuple2;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

//
//

//
//import org.dataalgorithms.util.SparkUtil;

/**
 * This class provides K-mer counting functionality.
 *
 * Kmer counting for a given K and N.
 * K: to find K-mers
 * N: to find top-N
 *
 * A kmer or k-mer is a short DNA sequence consisting of a fixed
 * number (K) of bases. The value of k is usually divisible by 4
 * so that a kmer can fit compactly into a basevector object.
 * Typical values include 12, 20, 24, 36, and 48; kmers of these
 * sizes are referred to as 12-mers, 20-mers, and so forth.
 *
 * @author Mahmoud Parsian
 * @author Brian O'Connor (made tweaks for HCA demo)
 *
 * TODO:
 * need to add UUID prefix and deal correctly with that in the final code
 *
 */

interface Retriable<T> {
    T run() throws Exception;
    static Random random = new Random();

    static <T> Optional<T> runWithRetries(int maxRetries, int maxInterval, Retriable<T> t) throws InterruptedException {
        int count = 0;
        while (count < maxRetries) {
            try {
                T value = t.run();
                return Optional.of(value);
            } catch (Exception e) {
                // simple backoff with jitter added
                Thread.sleep((count + 1) * 1000L + (long)(Retriable.random.nextDouble() * maxInterval));
                if (++count >= maxRetries) break;
            }
        }
        return Optional.empty();
    };
};


public class Kmer {

    // utility
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = Kmer.readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    // utility
    public static HttpURLConnection readLocationFromUrl(String url) throws IOException, InterruptedException {
        // TODO: this will need to deal with 301 code better
        String location = url;
        HttpURLConnection connection = null;
        int code = -1;
        int attempts = 0;
        while (true) {
            // attempt the request
            URL currentUrl = new URL(location);
            connection = (HttpURLConnection)currentUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            code = connection.getResponseCode();

            // if this is not a 301, return the connection
            if (code != 301) break;

            // else wait until retryAfter seconds is up
            int retryAfter = Integer.parseInt(connection.getHeaderField("retry-after"));
            System.err.println(String.format("301, retry-after %ds: %s", retryAfter, url));
            Thread.sleep(retryAfter * 1000L);
            location = connection.getHeaderField("location");
            attempts++;
            if (attempts >= 10) {
                // fail hard
                System.err.println("HTTP 301 Cycle loop!");
                System.exit(1);
            }
        }
        return connection;
    }

    // utility
    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    // utility
    public static ArrayList<String> streamAndFilterFastqGz(String uuid, String url, int numberOfLines) {

        // pattern matching
        Pattern pattern = Pattern.compile("^[atgcATGC]+$");

        ArrayList<String> result = new ArrayList<String>();
        // open up the uuid and stream back from it
        try {
            // TODO: URL is hard coded for production, need a parameter.
            Optional<HttpURLConnection> maybeConnection = Retriable.runWithRetries(
                    3,
                    2000,
                    () -> Kmer.readLocationFromUrl(url)
            );
            if (!maybeConnection.isPresent()) {
                // fail hard
                System.err.printf("GET FAILED: %s\n", url);
                System.exit(1);
            }
            HttpURLConnection connection = maybeConnection.get();
            InputStream is = connection.getInputStream();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(new GZIPInputStream(is), Charset.forName("UTF-8")));
                String line;
                int currLine = 0;
                while ( ( line = rd.readLine() ) != null ) {
                    if (pattern.matcher(line).matches() && (numberOfLines <= 0 || currLine < numberOfLines)) {
                        currLine++;
                        result.add(uuid+":::"+line);
                    }
                }
            } catch (Exception e) {
                System.err.println("ERROR READING FROM FILE URL 1: "+e.getMessage());
            } finally {
                is.close();
            }
        } catch (Exception e) {
            System.err.println("ERROR READING FROM FILE URL 2: "+e.getMessage());
        }
        return(result);
    }

    static ArrayList<Tuple2<String,String>> requestFileUrls(String uuid) throws Exception {
        // get bundle with retries or fail
        Optional<JSONObject> maybeJSON = Retriable.runWithRetries(
                3,
                2000,
                () -> Kmer.readJsonFromUrl("https://dss.data.humancellatlas.org/v1/bundles/" + uuid + "?replica=aws&presignedurls=true")
        );
        if (!maybeJSON.isPresent()) {
            // fail hard
            System.err.printf("GETTING BUNDLE %s FAILED\n", uuid);
            System.exit(1);
        }
        JSONObject json = maybeJSON.get();

        System.err.println("FROM THE JSON: "+((JSONObject)json.get("bundle")).get("creator_uid"));
        ArrayList<Tuple2<String,String>> results = new ArrayList<>();
        for (int i=0; i<((JSONObject)json.get("bundle")).getJSONArray("files").length(); i++ ) {
            JSONObject o = ((JSONObject)json.get("bundle")).getJSONArray("files").getJSONObject(i);
            String fileUrl = (String)o.get("url");
            String fileUuid = (String)o.get("uuid");
            if("application/gzip; dcp-type=data".equals((String)o.get("content-type"))) {
                results.add(new Tuple2<>(fileUuid, fileUrl));
            }
        }
        return results;
    }

    // main method
    public static void main(String[] args) throws Exception {
        // STEP-1: handle input parameters
        if (args.length < 5) {
            System.err.println("Usage: Kmer <manifest> <K> <N> <partitions> <outputPath>");
            System.exit(1);
        }
        final String manifestPath =  args[0];
        final int K =  Integer.parseInt(args[1]); // to find K-mers
        final int N =  Integer.parseInt(args[2]); // to find top-N
        final int partitionsNum =  Integer.parseInt(args[3]); // number of partitions to use
        final int numberOfLines =  Integer.parseInt(args[4]); // number of partitions to use
        final String outputPath =  args[5]; // output report path

        // STEP-2: create a Spark context object
        JavaSparkContext ctx = SparkUtil.createJavaSparkContext("kmer");

        // broadcast K and N as global shared objects,
        // which can be accessed from all cluster nodes
        final Broadcast<Integer> broadcastK = ctx.broadcast(K);
        final Broadcast<Integer> broadcastN = ctx.broadcast(N);

        // this is a manifest of UUIDs
        JavaRDD<String> manifestRecords = ctx.textFile(manifestPath, partitionsNum);
        //JavaRDD<String> manifestRecords = ctx.textFile(manifestPath);
        JavaRDD<Tuple2<String,String>> listOfFastqUrls = manifestRecords.flatMap(data -> requestFileUrls(data).iterator());
        listOfFastqUrls.repartition(partitionsNum).map(t -> t._1).saveAsTextFile(outputPath+"/uuids.tsv");

        // now generate fastqs lines prefixed with file UUID
        JavaRDD<String> filteredRDD = listOfFastqUrls.flatMap(t -> {
            ArrayList<String> result = Kmer.streamAndFilterFastqGz(t._1, t._2, numberOfLines);
            return(result.iterator());
        });

        // STEP-4: generate K-mers
        // PairFlatMapFunction<T, K, V>
        // T => Iterable<Tuple2<K, V>>
        JavaPairRDD<String,Integer> kmers = filteredRDD.flatMapToPair(new PairFlatMapFunction<
                String,        // T
                String,        // K
                Integer        // V
                >() {
            @Override
            public Iterator<Tuple2<String,Integer>> call(String sequence) {
                int K = broadcastK.value();
                List<Tuple2<String,Integer>> list = new ArrayList<Tuple2<String,Integer>>();
                String[] seqArr = sequence.split(":::");
                for (int i=0; i < seqArr[1].length()-K+1 ; i++) {
                    String kmer = seqArr[1].substring(i, K+i);
                    list.add(new Tuple2<String,Integer>(seqArr[0]+":::"+kmer, 1));
                }
                return list.iterator();
            }
        });

        // STEP-5: combine/reduce frequent kmers
        JavaPairRDD<String, Integer> kmersGrouped = kmers.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });

        // now, we have: (K=kmer,V=frequency)
        // next step is find the top-N kmers
        // create a local top-N
        JavaRDD<Map<String, SortedMap<Integer, String>>> partitions = kmersGrouped.mapPartitions(
            new FlatMapFunction<Iterator<Tuple2<String,Integer>>, Map<String, SortedMap<Integer, String>>>() {
                @Override
                public Iterator<Map<String, SortedMap<Integer, String>>> call(Iterator<Tuple2<String,Integer>> iter) {
                    int N = broadcastN.value();
                    Map<String, SortedMap<Integer, String>> topNForUUID = new HashMap<String, SortedMap<Integer, String>>();

                    while (iter.hasNext()) {
                        Tuple2<String,Integer> tuple = iter.next();
                        String kmer = tuple._1;
                        String[] kmerStr = kmer.split(":::");
                        int frequency = tuple._2;
                        SortedMap<Integer, String> topN = null;
                        if (topNForUUID.get(kmerStr[0]) == null) {
                            topN = new TreeMap<Integer, String>();
                            topNForUUID.put(kmerStr[0], topN);
                        } else {
                            topN = topNForUUID.get(kmerStr[0]);
                        }
                        topN.put(frequency, kmerStr[1]);
                        // keep only top N
                        if (topN.size() > N) {
                            topN.remove(topN.firstKey());
                        }
                    }
                    //System.out.println("topN="+topN);
                    //return Collections.singletonList(topN).iterator();
                    return(Collections.singletonList(topNForUUID).iterator());
                }
            });

        // now collect all topN from all partitions
        // and find topN from all partitions
        Map<String, SortedMap<Integer, String>> finalTopNForUUID = new HashMap<String, SortedMap<Integer, String>>();
        List<Map<String, SortedMap<Integer, String>>> alltopN = partitions.collect();
        for (Map<String, SortedMap<Integer, String>> localtopN : alltopN) {
            for (String uuid : localtopN.keySet()) {
                // frequency = tuple._1
                // kmer = tuple._2
                SortedMap<Integer, String> finaltopN = new TreeMap<Integer, String>();
                for (Map.Entry<Integer, String> entry : localtopN.get(uuid).entrySet()) {
                    finaltopN.put(entry.getKey(), entry.getValue());
                    // keep only top N
                    if (finaltopN.size() > N) {
                        finaltopN.remove(finaltopN.firstKey());
                    }
                }
                finalTopNForUUID.put(uuid, finaltopN);
            }
        }

        // emit final topN descending
        ArrayList<String> finalResults = new ArrayList<String>();
        for (String uuid : finalTopNForUUID.keySet()) {
            System.out.println("=== top " + N + " kmers for " + uuid + " ===");
            List<Integer> frequencies = new ArrayList<Integer>(finalTopNForUUID.get(uuid).keySet());
            for (int i = frequencies.size() - 1; i >= 0; i--) {
                System.out.println(frequencies.get(i) + "\t" + finalTopNForUUID.get(uuid).get(frequencies.get(i)));
                finalResults.add(uuid + "\t" + frequencies.get(i) + "\t" + finalTopNForUUID.get(uuid).get(frequencies.get(i)));
            }
        }

        JavaRDD<String> finalResultsRDD = ctx.parallelize(finalResults);
        finalResultsRDD.saveAsTextFile(outputPath+"/top_kmers.tsv");

        // I'm commenting these out, they cause the EMR job to fail if I leave either in here!
        //ctx.close();
        //System.exit(0);
    }
}