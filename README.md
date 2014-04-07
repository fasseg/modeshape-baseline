Usage
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

