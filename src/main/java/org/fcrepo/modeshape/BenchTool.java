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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uncommons.maths.random.XORShiftRNG;

import javax.jcr.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;

public class BenchTool {
    private static final Logger log = LoggerFactory.getLogger(BenchTool.class);
    private static final DecimalFormat FORMAT = new DecimalFormat("###.##");

    public void runBenchMark(Repository repo, int num, long size, int threads) throws Exception {
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<Result>> futs = new ArrayList<Future<Result>>();
        for (int i = 0; i < num; i++) {
            futs.add(executorService.submit(new Ingester(size, repo)));
        }
        long sumDurations = 0l;
        long sumSizes = 0l;
        for (Future<Result> f : futs) {
            Result res = f.get();
            sumDurations += res.getDuration();
            sumSizes += res.getSize();
        }
        log.info("Benchmark finished.");
        log.info("Overall throughput {} mb/sec", FORMAT.format((float) sumSizes / (float) sumDurations * 1000 / (1024 * 1024)));
        log.info("Overall size {}", convertSize(sumSizes));
        log.info("Overall duration {} secs", sumDurations/1000l);
    }

    public static String convertSize(final long size) {
        final int unit = 1024;
        if (size < unit) {
            return size + " B";
        }
        final int exp = (int) (Math.log(size) / Math.log(unit));
        final char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %cB", size / Math.pow(unit, exp), pre);
    }

    static class Ingester implements Callable<Result> {

        private final long size;
        private final Repository repo;

        Ingester(long size, Repository repo) {
            this.size = size;
            this.repo = repo;
        }

        @Override
        public Result call() throws Exception {
            long time = System.currentTimeMillis();
            Session sess = repo.login();
            Node root = sess.getRootNode();
            Node folder = root.addNode("folder-" + UUID.randomUUID().toString(), "nt:folder");
            Node file = folder.addNode("file-" + UUID.randomUUID().toString(), "nt:file");
            Node binary = file.addNode("jcr:content", "nt:resource");
            binary.setProperty("jcr:data", sess.getValueFactory().createBinary(new RandomInputStream(size)));
            sess.save();
            long duration = System.currentTimeMillis() - time;
            float tp = (float) size / (float) duration;
            log.info("Throughput:{}/{} bytes/ms = {} mb/sec", new Object[]{(Long) size, (Long) duration, FORMAT.format(tp * 1000f / (1024f * 1024f))});
            log.info("Time spent: {} ms", duration);
            return new Result(tp, size, duration);
        }
    }

    static class RandomInputStream extends InputStream {

        public static final XORShiftRNG RNG = new XORShiftRNG();

        private final long size;
        private long read = 0l;

        RandomInputStream(long size) {
            this.size = size;
        }

        @Override
        public int read() throws IOException {
            if (read++ >= size) {
                return -1;
            }
            return RNG.nextInt(Integer.MAX_VALUE - 1) + 1;
        }
    }

    static class Result {
        private final float throughput;
        private final long size;
        private final long duration;

        Result(float throughput, long size, long duration) {
            this.throughput = throughput;
            this.size = size;
            this.duration = duration;
        }

        public long getSize() {
            return size;
        }

        public long getDuration() {
            return duration;
        }

        public float getThroughput() {

            return throughput;
        }
    }
}
