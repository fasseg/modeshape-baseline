/* 
* Copyright 2014 Frank Asseg
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*/
package org.fcrepo.modeshape;

import org.apache.commons.cli.*;
import org.infinispan.schematic.document.ParsingException;
import org.modeshape.jcr.ModeShapeEngine;
import org.modeshape.jcr.RepositoryConfiguration;

import javax.jcr.Repository;
import java.awt.*;
import java.net.URI;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModeshapeServer {

    public static void main(String[] args) {
        /* setup the command line options */
        final Options ops = createOptions();

        /* set the defaults */
        int numBinaries = 1;
        long size = 1024;
        int numThreads = 1;
        String logPath = "durations.log";
        boolean benchEnabled = false;

        /* and get the individual settings from the command line */
        final CommandLineParser parser = new BasicParser();
        try {
            final CommandLine cli = parser.parse(ops, args);
            if (cli.hasOption("h")) {
                printUsage(ops);
                return;
            }
            if (cli.hasOption("n")) {
                numBinaries = Integer.parseInt(cli.getOptionValue("n"));
            }
            if (cli.hasOption("s")) {
                size = getSizeFromArgument(cli.getOptionValue("s"));
            }
            if (cli.hasOption("t")) {
                numThreads = Integer.parseInt(cli.getOptionValue("t"));
            }
            if (cli.hasOption("l")) {
                logPath = cli.getOptionValue("l");
            }
            if (cli.hasOption("b")) {
                benchEnabled = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }
        ModeshapeServer server = new ModeshapeServer();
        try {
            server.start(benchEnabled,numBinaries, size, numThreads);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start(boolean benchEnabled, int num, long size, int threads) throws Exception {
        RepositoryConfiguration cfg = RepositoryConfiguration.read(this.getClass().getClassLoader().getResourceAsStream("repository.json"), "repo");
        ModeShapeEngine engine = new ModeShapeEngine();
        engine.start();
        Repository repo = engine.deploy(cfg);
        repo.login();
        while (engine.getRepositoryState("repo") != ModeShapeEngine.State.RUNNING) {
            Thread.yield();
        }
        System.out.println("Modeshape is up and running.");
        if (benchEnabled) {
            System.out.println("Enter 'S' to start the tests:");
            Scanner scan = new Scanner(System.in);
            while (!scan.hasNextLine()) {
                Thread.yield();
            }
            String input = scan.next();
            if (input.equalsIgnoreCase("s")) {
                System.out.println("starting tests");
                BenchTool tool = new BenchTool();
                tool.runBenchMark(repo, num, size, threads);
            } else {
                System.out.println("stopping server");
            }
        }
    }

    @SuppressWarnings("static-access")
    private static Options createOptions() {
        final Options ops = new Options();
        ops.addOption(OptionBuilder.withArgName("num-actions").withDescription(
                "The number of actions performed. [default=1]").withLongOpt("num-actions").hasArg().create('n'));
        ops.addOption(OptionBuilder
                .withArgName("size")
                .withDescription(
                        "The size of the individual binaries used. Sizes with a k,m,g or t postfix will be interpreted as kilo-, mega-, giga- and terabyte [default=1024]")
                .withLongOpt("size").hasArg().create('s'));
        ops.addOption(OptionBuilder.withArgName("num-threads").withDescription(
                "The number of threads used for performing all actions. [default=1]").withLongOpt("num-threads")
                .hasArg().create('t'));
        ops.addOption(OptionBuilder.withArgName("log").withDescription(
                "The log file to which the durations will get written. [default=durations.log]").withLongOpt("log")
                .hasArg().create('l'));
        ops.addOption(OptionBuilder.withDescription(
                "Enable the running the benchmark on this node").withLongOpt("bench")
                .create('b'));
        return ops;
    }

    public static void printUsage(final Options ops) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ModeshapeTest", ops);
    }

    private static long getSizeFromArgument(final String optionValue) {
        final Matcher m = Pattern.compile("^(\\d*)([kKmMgGtT]{0,1})$").matcher(optionValue);
        if (!m.find()) {
            throw new IllegalArgumentException("Size " + optionValue + " could not be parsed");
        }
        final long size = Long.parseLong(m.group(1));
        if (m.groupCount() == 1) {
            return size;
        }
        final char postfix = m.group(2).charAt(0);
        switch (postfix) {
            case 'k':
            case 'K':
                return size * 1024l;
            case 'm':
            case 'M':
                return size * 1024l * 1024l;
            case 'g':
            case 'G':
                return size * 1024l * 1024l * 1024l;
            case 't':
            case 'T':
                return size * 1024l * 1024l * 1024l * 1024l;
            default:
                return size;
        }
    }
}
