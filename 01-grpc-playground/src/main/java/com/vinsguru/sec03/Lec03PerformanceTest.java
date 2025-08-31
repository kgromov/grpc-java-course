package com.vinsguru.sec03;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.runtime.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import com.owlike.genson.Genson;
import com.vinsguru.models.sec03.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class Lec03PerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(Lec03PerformanceTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<Class<?>, Yaml> yamlCache = new HashMap<>();
    private static final Gson gson = new Gson();
    private static final Genson genson = new Genson();
    private static final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().includeServiceLoader());
    private static final com.dslplatform.json.JsonWriter writer = dslJson.newWriter();

    public static void main(String[] args) {

        var protoPerson = Person.newBuilder()
                .setLastName("sam")
                .setAge(12)
                .setEmail("sam@gmail.com")
                .setEmployed(true)
                .setSalary(1000.2345)
                .setBankAccountNumber(123456789012L)
                .setBalance(-10000)
                .build();
        var jsonPerson = new JsonPersonRecord("sam", 12, "sam@gmail.com", true, 1000.2345, 123456789012L, -10000);
        var jsonPerson2 = new JsonPerson("sam", 12, "sam@gmail.com", true, 1000.2345, 123456789012L, -10000);
        /*
            Trivial benchmarks for 1M objects:
            proto bytes length: 44;
            json bytes length: 136;
            yaml bytes length: 134;

            proto: LongSummaryStatistics{count=5, sum=1350, min=128, average=270.000000, max=692}
            dslJson: LongSummaryStatistics{count=5, sum=3988, min=766, average=797.600000, max=873}
            jackson: LongSummaryStatistics{count=5, sum=6026, min=1107, average=1205.200000, max=1558}
            gson: LongSummaryStatistics{count=5, sum=12282, min=2322, average=2456.400000, max=2597}
            genson: LongSummaryStatistics{count=5, sum=14795, min=2794, average=2959.000000, max=3228}
            jsonio: jsonio: LongSummaryStatistics{count=5, sum=69550, min=13609, average=13910.000000, max=14241}
            yaml:  yaml: LongSummaryStatistics{count=5, sum=71444, min=13559, average=14288.800000, max=15070} (string)
         */
        List<Metrics> metrics = List.of(
                new Metrics("proto", () -> proto(protoPerson)),
                new Metrics("jackson",  () -> jackson(jsonPerson, JsonPersonRecord.class)),
                new Metrics("gson", () -> gson(jsonPerson, JsonPersonRecord.class)),
                new Metrics("genson", () -> genson(jsonPerson2, JsonPerson.class)),
                new Metrics("jsonio", () -> jsonio(jsonPerson2, JsonPerson.class)),
                new Metrics("yaml", () -> yaml(jsonPerson2, JsonPerson.class)),
                new Metrics("dslJson", () -> dslJson(jsonPerson, JsonPersonRecord.class))
//                new Metrics("dslJson2", () -> dslJson2(jsonPerson, JsonPersonRecord.class))
        );

        for (int i = 0; i < 5; i++) {
            metrics.forEach(metric -> metric.statistics().accept(runTest(metric.name(), metric.method())));
        }
        metrics.forEach(metric -> log.info("{}: {}", metric.name(), metric.statistics()));
    }

    private static void proto(Person person) {
        try {
            var bytes = person.toByteArray();
            log.trace("proto bytes length: {}", bytes.length);
            Person.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void jackson(T person, Class<T> clazz) {
        try {
            var bytes = mapper.writeValueAsBytes(person);
            log.trace("json bytes length: {}", bytes.length);
            mapper.readValue(bytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void gson(T person, Class<T> clazz) {
        try {
            String json = gson.toJson(person);
            log.trace("json bytes length: {}", json.getBytes().length);
            gson.fromJson(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void jsonio(T person, Class<T> clazz) {
        try {
            String json = JsonWriter.objectToJson(person);
            JsonReader.jsonToJava(json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void genson(T person, Class<T> clazz) {
        try {
            byte[] bytes = genson.serializeBytes(person);
            log.trace("genson bytes length: {}", bytes.length);
            genson.deserialize(bytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void yaml(T person, Class<T> clazz) {
        Yaml yaml = yamlCache.computeIfAbsent(clazz, value -> new Yaml(new Constructor(clazz, new LoaderOptions())));
        try {
            String content = yaml.dumpAs(person, Tag.MAP, null);
            byte[] bytes = content.getBytes();
            log.trace("yaml bytes length: {}", bytes.length);
            yaml.load(content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void dslJson(T person, Class<T> clazz) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            dslJson.serialize(person, os);
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            T object = dslJson.deserialize(clazz, is);
            int a = 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void dslJson2(T object, Class<T> clazz) {
        try {
            dslJson.serialize(writer, object);
            byte[] buffer = writer.getByteBuffer();
            T value = dslJson.deserialize(clazz, buffer, writer.size());
            int a = 1;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static long runTest(String testName, Runnable runnable) {
        var start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            runnable.run();
        }
        var end = System.currentTimeMillis();
        return end - start;
    }

    private record Metrics(String name, Runnable method, LongSummaryStatistics statistics) {
        public Metrics(String name, Runnable method) {
            this(name, method, new LongSummaryStatistics());
        }
    }
}
