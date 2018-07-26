package kmer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.broadcast.Broadcast;
import scala.Tuple2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


/**
 * This class provides K-mer counting functionality.
 * <p>
 * Kmer counting for a given K and N.
 * K: to find K-mers
 * N: to find top-N
 * <p>
 * A kmer or k-mer is a short DNA sequence consisting of a fixed
 * number (K) of bases. The value of k is usually divisible by 4
 * so that a kmer can fit compactly into a basevector object.
 * Typical values include 12, 20, 24, 36, and 48; kmers of these
 * sizes are referred to as 12-mers, 20-mers, and so forth.
 *
 * @author Mahmoud Parsian
 * @author Brian O'Connor (made tweaks for HCA demo)
 * @author Matt Weiden (added some tests)
 * <p>
 * TODO:
 * need to add UUID prefix and deal correctly with that in the final code
 */
public class Kmer {

    public static ArrayList<String> streamAndFilterFastqGz(String uuid, String url, int numberOfLines) {

        Pattern pattern = Pattern.compile("^[atgcATGC]+$");

        ArrayList<String> result = new ArrayList<String>();
        // open up the uuid and stream back from it
        try {
            // TODO: URL is hard coded for production, need a parameter.
            Optional<HttpURLConnection> maybeConnection = Retriable.runWithRetries(
                    3,
                    2000,
                    () -> HttpUtil.readLocationFromUrl(url)
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
                while ((line = rd.readLine()) != null) {
                    if (pattern.matcher(line).matches() && (numberOfLines <= 0 || currLine < numberOfLines)) {
                        currLine++;
                        result.add(uuid + ":::" + line);
                    }
                }
            } catch (Exception e) {
                System.err.println("ERROR READING FROM FILE URL 1: " + e.getMessage());
            } finally {
                is.close();
            }
        } catch (Exception e) {
            System.err.println("ERROR READING FROM FILE URL 2: " + e.getMessage());
        }
        return (result);
    }

    private static class Args implements Serializable {

        @Parameter
        private List<String> parameters = new ArrayList<>();

        @Parameter(names = { "--endpoint", "-e" }, description = "DSS endpoint")
        private String dssEndpoint;

        @Parameter(names = { "--manifest-path", "-m" }, description = "Filepath to manifest")
        private String manifestPath;

        @Parameter(names = { "-k" }, description = "Base pairs in Kmers")
        private Integer K = 1;

        @Parameter(names = { "-n" }, description = "Top N kmers")
        private Integer N = 1;

        @Parameter(names = { "--number-of-partitions", "-p" }, description = "Number of partitions")
        private Integer partitionsNum = 1;

        @Parameter(names = { "--number-of-lines", "-l" }, description = "Number of lines")
        private Integer numberOfLines = 1;

        @Parameter(names = { "--output-path", "-o" }, description = "Output path")
        private String outputPath;

        @Parameter(names = { "--help", "-h" }, help = true)
        private boolean help;
    }

    // main method
    public static void main(String[] argv) throws Exception {
        Args args = new Args();

        JCommander jCommander = JCommander.newBuilder()
                .addObject(args)
                .build();

        jCommander.setProgramName("Kmer");
        jCommander.parse(argv);
        if (args.help) {
            jCommander.usage();
            return;
        }

        Kmer.run(args);
    }

    private static void run(Args args) throws Exception {
        DataStoreClient dataStoreClient = new DataStoreClient(args.dssEndpoint);

        // STEP-2: create a Spark context object
        JavaSparkContext ctx = SparkUtil.createJavaSparkContext("kmer");

        // broadcast K and N as global shared objects,
        // which can be accessed from all cluster nodes
        final Broadcast<Integer> broadcastK = ctx.broadcast(args.K);
        final Broadcast<Integer> broadcastN = ctx.broadcast(args.N);

        // this is a manifest of UUIDs
        JavaRDD<String> manifestRecords = ctx.textFile(args.manifestPath, args.partitionsNum);
        //JavaRDD<String> manifestRecords = ctx.textFile(manifestPath);
        JavaRDD<Tuple2<String, String>> listOfFastqUrls = manifestRecords.flatMap(data -> DataStore.requestFileUrls(data).iterator());

        ctx.parallelize(listOfFastqUrls.map(t -> t._1).collect()).saveAsTextFile(args.outputPath + "/uuids");

        // now generate fastqs lines prefixed with file UUID
        JavaRDD<String> filteredRDD = listOfFastqUrls.flatMap(t -> {
            ArrayList<String> result = Kmer.streamAndFilterFastqGz(t._1, t._2, args.numberOfLines);
            return (result.iterator());
        });

        // STEP-4: generate K-mers
        // PairFlatMapFunction<T, K, V>
        // T => Iterable<Tuple2<K, V>>
        JavaPairRDD<String, Integer> kmers = filteredRDD.flatMapToPair(new PairFlatMapFunction<
                String,        // T
                String,        // K
                Integer        // V
                >() {
            @Override
            public Iterator<Tuple2<String, Integer>> call(String sequence) {
                int K = broadcastK.value();
                List<Tuple2<String, Integer>> list = new ArrayList<Tuple2<String, Integer>>();
                String[] seqArr = sequence.split(":::");
                for (int i = 0; i < seqArr[1].length() - K + 1; i++) {
                    String kmer = seqArr[1].substring(i, K + i);
                    list.add(new Tuple2<String, Integer>(seqArr[0] + ":::" + kmer, 1));
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
                new FlatMapFunction<Iterator<Tuple2<String, Integer>>, Map<String, SortedMap<Integer, String>>>() {
                    @Override
                    public Iterator<Map<String, SortedMap<Integer, String>>> call(Iterator<Tuple2<String, Integer>> iter) {
                        int N = broadcastN.value();
                        Map<String, SortedMap<Integer, String>> topNForUUID = new HashMap<String, SortedMap<Integer, String>>();

                        while (iter.hasNext()) {
                            Tuple2<String, Integer> tuple = iter.next();
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
                        return (Collections.singletonList(topNForUUID).iterator());
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
                    if (finaltopN.size() > args.N) {
                        finaltopN.remove(finaltopN.firstKey());
                    }
                }
                finalTopNForUUID.put(uuid, finaltopN);
            }
        }

        // emit final topN descending
        ArrayList<String> finalResults = new ArrayList<String>();
        for (String uuid : finalTopNForUUID.keySet()) {
            System.out.println("=== top " + args.N + " kmers for " + uuid + " ===");
            List<Integer> frequencies = new ArrayList<Integer>(finalTopNForUUID.get(uuid).keySet());
            for (int i = frequencies.size() - 1; i >= 0; i--) {
                System.out.println(frequencies.get(i) + "\t" + finalTopNForUUID.get(uuid).get(frequencies.get(i)));
                finalResults.add(uuid + "\t" + frequencies.get(i) + "\t" + finalTopNForUUID.get(uuid).get(frequencies.get(i)));
            }
        }

        JavaRDD<String> finalResultsRDD = ctx.parallelize(finalResults);
        finalResultsRDD.saveAsTextFile(args.outputPath + "/top_kmers");

        // I'm commenting these out, they cause the EMR job to fail if I leave either in here!
        //ctx.close();
        //System.exit(0);
    }
}