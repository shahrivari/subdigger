package org.tmu.subdigger;

import com.google.common.base.Stopwatch;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Saeed on 3/9/14.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        CommandLineParser parser = new BasicParser();
        //System.in.read();

        // create the Options
        Options options = new Options();
        options.addOption("i", "input", true, "the input file name.");
        options.addOption("s", "size", true, "size of subgraphs to enumerate.");
        options.addOption("o", "output", true, "the output file name (default out.txt)");
        options.addOption("um", true, "the max size of unique map.");
        options.addOption("lm", true, "the max size of label map.");
        options.addOption("mc", true, "maximum subgraphs count to stop. Default is infinity i.e. enumerate all.");
        options.addOption("n", "nonisomorphic", false, "enumerate just nonisomorphic subgraphs.");
        options.addOption("c", "count", false, "count all subgraphs");
        options.addOption("t", "threads", true, "number of threads to use");
        options.addOption("silent", false, "suppress progress report.");
        HelpFormatter formatter = new HelpFormatter();


        String input_path = "";
        String output_path = "out.txt";
        Graph graph = null;
        Stopwatch stopwatch = Stopwatch.createStarted();
        int size = 3;
        int threads = Runtime.getRuntime().availableProcessors();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("s")) {
                size = Integer.parseInt(line.getOptionValue("s"));
                if (size < 3) {
                    System.out.println("Size of subgraphs must be greater or equal to 3.");
                    System.exit(-1);
                }
            }

            if (line.hasOption("t")) {
                threads = Integer.parseInt(line.getOptionValue("t"));
            }


            if (line.hasOption("o")) {
                output_path = line.getOptionValue("o");
            }

            if (!line.hasOption("i")) {
                System.out.println("An input file must be given.");
                formatter.printHelp("subdigger", options);
                System.exit(-1);
            } else {
                input_path = line.getOptionValue("i");

                graph = HashGraph.readStructureFromFile(input_path);
                if (graph.vertexCount() < 20000) {
                    System.out.printf("Graph is small. Using adjacency matrix.\n");
                    graph = MatGraph.readStructureFromFile(input_path);
                }


                System.out.printf("Graph loaded in %s msecs.\n", stopwatch.elapsed(TimeUnit.MILLISECONDS));
                graph.printInfo();
                stopwatch.reset().start();

                if (line.hasOption("mc")) {
                    SMPEnumerator.setMaxCount(Long.parseLong(line.getOptionValue("mc")));
                    System.out.printf("Subgraph count is bounded to %,d.\n", SMPEnumerator.getMaxCount());
                } else
                    SMPEnumerator.setMaxCount(Long.MAX_VALUE);


                if (line.hasOption("um")) {
                    SMPEnumerator.setUniqueCap(Integer.parseInt(line.getOptionValue("um")));
                }
                System.out.printf("Unique map size: %,d.\n", SMPEnumerator.getUniqueCap());


                if (line.hasOption("lm")) {
                    SignatureRepo.setCapacity(Integer.parseInt(line.getOptionValue("lm")));
                }
                System.out.printf("Label map size: %,d.\n", SignatureRepo.getCapacity());

                if (line.hasOption("silent"))
                    SMPEnumerator.setVerbose(false);
                else
                    SMPEnumerator.setVerbose(true);

                System.out.printf("Totally found: %,d subgraphs\n", SMPEnumerator.enumerateNonIsoInParallel(graph, size, threads, output_path));
                System.out.println("File: " + input_path + " \t #subsize:" + size);
                System.out.println("Took: " + stopwatch + " \tequal to " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds.");
            }


        } catch (org.apache.commons.cli.ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
            formatter.printHelp("subdigger", options);
            System.exit(-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
