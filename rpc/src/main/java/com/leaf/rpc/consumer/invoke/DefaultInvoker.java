package com.leaf.rpc.consumer.invoke;

import com.leaf.common.model.ServiceMeta;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.StrategyConfig;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;

public class DefaultInvoker extends AbstractInvoker {

    public DefaultInvoker(
                String application,
                Dispatcher dispatcher,
                ServiceMeta serviceMeta,
                StrategyConfig strategyConfig,
                InvokeType invokeType) {
        super(application, dispatcher, serviceMeta, strategyConfig, invokeType);
    }

    @RuntimeType
    public Object invoke(@Origin Method method, @AllArguments @RuntimeType Object[] args) throws Throwable {
        return doInvoke(method.getName(), args);
    }

}
