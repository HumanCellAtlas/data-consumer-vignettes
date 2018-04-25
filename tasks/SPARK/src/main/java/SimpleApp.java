/* SimpleApp.java */
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.api.java.JavaSparkContext;

public class SimpleApp {
    public static void main(String[] args) {

        String url = "s3://hca-data-bundles/seq/GSE81904/SRR3587502/SRR3587502_1.fastq.gz";
        if (args.length > 0) {
            url = args[0];
        }

        /*
        String prefix = "s3a://" + properties.get("s3.source.bucket") + "/";
objectListing.getObjectSummaries().forEach(summary -> keys.add(prefix+summary.getKey()));
// repeat while objectListing truncated
JavaRDD<String> events = sc.textFile(String.join(",", keys))
https://stackoverflow.com/questions/41062705/reading-multiple-files-from-s3-in-parallel-spark-java
         */

        //https://sparkour.urizone.net/recipes/using-s3/
        // Save data to S3 using the s3a protocol
        //localRdd.saveAsTextFile("s3a://sparkour-data/output-path/");

        // Create an RDD from a file in S3 using the s3a protocol
        //JavaRDD<String> s3aRdd = sc.textFile("s3a://sparkour-data/random_numbers.txt");

        String logFile = "YOUR_SPARK_HOME/README.md"; // Should be some file on your system
        SparkSession spark = SparkSession.builder().appName("Simple Application").getOrCreate();
        JavaSparkContext sc = new JavaSparkContext(spark.sparkContext());

        // seems like compressed files should work fine: https://stackoverflow.com/questions/16302385/gzip-support-in-spark
        //Dataset<String> logData = spark.read().textFile(logFile).cache();
        JavaRDD<String> logData = sc.textFile(url);

        long numAs = logData.filter(s -> s.contains("A")).count();
        long numCs = logData.filter(s -> s.contains("C")).count();

        System.out.println("Lines with A: " + numAs + ", lines with C: " + numCs);

        spark.stop();
    }
}