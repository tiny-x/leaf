package com.leaf.example.benchmark;

import com.leaf.example.demo.ConsumerExample;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@Threads(5)
public class Benchmark {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Benchmark.class.getSimpleName())
                .forks(1)
                .warmupIterations(10) //预热次数
                .measurementIterations(10) //真正执行次数
                .build();

        new Runner(opt).run();
    }

    private static ConsumerExample consumerExample = new ConsumerExample();

    @org.openjdk.jmh.annotations.Benchmark
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void call() {
       consumerExample.invoke();
    }

}
