This project creates an executable jar that starts a Modeshape Server. If the bench (-b) option is set a Modeshape Server is started with an internal benchmarking process.
This of course only tests a non load balanced scenario, but it does demonstrate the write performance of a single node in a clustered environment.

Configuration
---
[infinispan.xml](https://github.com/fasseg/modeshape-baseline/blob/master/src/main/resources/infinispan.xml)
[jgroups.xml](https://github.com/fasseg/modeshape-baseline/blob/master/src/main/resources/jgroups.xml)
[repository.json](https://github.com/fasseg/modeshape-baseline/blob/master/src/main/resources/repository.json)

Usage
---
```
 -b,--bench                       Enable the running the benchmark on this
                                  node
 -h,--help                        print the help screen
 -l,--log <log>                   The log file to which the durations will
                                  get written. [default=durations.log]
 -n,--num-actions <num-actions>   The number of actions performed.
                                  [default=1]
 -s,--size <size>                 The size of the individual binaries
                                  used. Sizes with a k,m,g or t postfix
                                  will be interpreted as kilo-, mega-,
                                  giga- and terabyte [default=1024]
 -t,--num-threads <num-threads>   The number of threads used for
                                  performing all actions. [default=1]
```
Example
---

Build the executable jar using

```bash
mvn clean package
```

Run nodes using the executable jar on different machines:

```bash
java -jar target/modeshape-baseline-1.0-SNAPSHOT.jar
```

Start a single node with the benchmark option (-b) set:

```bash
java -jar target/modeshape-baseline-1.0-SNAPSHOT.jar -n 50 -s 1m -t 5 -b
```

This will start an ingest of 50 files of size 1 megabyte using 5 threads and print out the results.

