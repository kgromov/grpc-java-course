package com.vinsguru.sec03;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
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

import java.util.HashMap;
import java.util.LongSummaryStatistics;
import java.util.Map;

public class Lec03PerformanceTest {

    private static final Logger log = LoggerFactory.getLogger(Lec03PerformanceTest.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<Class<?>, Yaml> yamlCache = new HashMap<>();
    private static final Gson gson = new Gson();
    private static final Genson genson = new Genson();

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
            json bytes length: 136;
            yaml bytes length: 134;
            proto bytes length: 44;
         */
//        json(jsonPerson, JsonPersonRecord.class);
//        yaml(jsonPerson2, JsonPerson.class);
//        proto(protoPerson);
//        System.exit(1);

        /*
            Trivial benchmarks for 1M objects:
            proto: LongSummaryStatistics{count=5, sum=1350, min=128, average=270.000000, max=692}
            jackson: LongSummaryStatistics{count=5, sum=6026, min=1107, average=1205.200000, max=1558}
            gson: LongSummaryStatistics{count=5, sum=12282, min=2322, average=2456.400000, max=2597}
            genson: LongSummaryStatistics{count=5, sum=14795, min=2794, average=2959.000000, max=3228}
            jsonio: jsonio: LongSummaryStatistics{count=5, sum=69550, min=13609, average=13910.000000, max=14241}
            yaml:  yaml: LongSummaryStatistics{count=5, sum=71444, min=13559, average=14288.800000, max=15070} (string)
         */
        var jackson = new LongSummaryStatistics();
        var gson = new LongSummaryStatistics();
        var genson = new LongSummaryStatistics();
        var proto = new LongSummaryStatistics();
        var yaml = new LongSummaryStatistics();
        var jsonio = new LongSummaryStatistics();
        for (int i = 0; i < 5; i++) {
            proto.accept(runTest("proto", () -> proto(protoPerson)));
            jackson.accept(runTest("json", () -> jackson(jsonPerson, JsonPersonRecord.class)));
            gson.accept(runTest("gson", () -> gson(jsonPerson, JsonPersonRecord.class)));
            genson.accept(runTest("genson", () -> genson(jsonPerson2, JsonPerson.class)));    // does not support records
            genson.accept(runTest("jsonio", () -> genson(jsonPerson2, JsonPerson.class)));    // does not support records
            yaml.accept(runTest("yaml", () -> jsonio(jsonPerson2, JsonPerson.class)));  // does not support records
        }
        log.info("jackson: {}", jackson);
        log.info("gson: {}", gson);
        log.info("genson: {}", genson);
        log.info("proto: {}", proto);
        log.info("jsonio: {}", jsonio);
        log.info("yaml: {}", yaml);
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

    private static long runTest(String testName, Runnable runnable) {
        var start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            runnable.run();
        }
        var end = System.currentTimeMillis();
        return end - start;
    }

}
