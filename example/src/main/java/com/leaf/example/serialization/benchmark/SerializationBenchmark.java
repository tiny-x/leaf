package com.leaf.example.serialization.benchmark;

import com.leaf.serialization.api.Serializer;
import com.leaf.serialization.api.SerializerFactory;
import com.leaf.serialization.api.SerializerType;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Threads(5)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SerializationBenchmark {

    private static final Serializer javaSerializer = SerializerFactory.serializer(SerializerType.JAVA);
    private static final Serializer protostuffSerializer = SerializerFactory.serializer(SerializerType.PROTO_STUFF);
    private static final Serializer hessianSerializer = SerializerFactory.serializer(SerializerType.HESSIAN);
    private static final Serializer jsonSerializer = SerializerFactory.serializer(SerializerType.FAST_JSON);

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SerializationBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    private User newUser() {
        User user = new User(1, "小飞鱼");
        user.setBirthday(new Date());
        user.setMobilePhone("13136108286");
        user.setPermission(Arrays.asList(new String[]{"add", "select", "edit", "delete", "system"}));
        user.setUserInfo("ha ha ha ha ha ha ha ha ha ha ha ha ha ha ha ha ! 大家好");
        return user;
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void javaSerializer() {

        byte[] bytes = javaSerializer.serialize(newUser());
        javaSerializer.deserialize(bytes, User.class);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void protostuffSerializer() {
        byte[] bytes = protostuffSerializer.serialize(newUser());
        protostuffSerializer.deserialize(bytes, User.class);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void hessianSerializer() {
        byte[] bytes = hessianSerializer.serialize(newUser());
        hessianSerializer.deserialize(bytes, User.class);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void jsonSerializer() {
        byte[] bytes = jsonSerializer.serialize(newUser());
        jsonSerializer.deserialize(bytes, User.class);
    }
}
