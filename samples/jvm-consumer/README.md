# JVM consumer sample

This sample proves the SDK can be used from outside, through published artifacts and public APIs only.

```bash
cd ../..
```

```bash
./gradlew publishToMavenLocal
```

```bash
cd samples/jvm-consumer
```

```bash
../../gradlew run
```

The same conversion from Java ([`JavaMain.java`](src/main/java/sample/JavaMain.java)):

```bash
../../gradlew runJava
```

The Android sample app follows in Sprint 6.
