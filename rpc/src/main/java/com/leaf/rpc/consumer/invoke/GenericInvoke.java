package com.leaf.rpc.consumer.invoke;

import com.leaf.common.model.ServiceMeta;
import com.leaf.rpc.consumer.InvokeType;
import com.leaf.rpc.consumer.StrategyConfig;
import com.leaf.rpc.consumer.dispatcher.Dispatcher;

public class GenericInvoke extends AbstractInvoker {

    public GenericInvoke(
            String application,
            Dispatcher dispatcher,
            ServiceMeta serviceMeta,
            StrategyConfig strategyConfig,
            InvokeType invokeType) {
        super(application, dispatcher, serviceMeta, strategyConfig, invokeType);
    }

    public Object $invoke(String methodName, Object... args) throws Throwable{
        return doInvoke(methodName, args);
    }
}
