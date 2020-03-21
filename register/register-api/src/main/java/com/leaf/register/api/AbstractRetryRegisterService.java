package com.leaf.register.api;

import com.leaf.register.api.model.RegisterMeta;
import com.leaf.register.api.model.SubscribeMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class AbstractRetryRegisterService implements RegisterService {

    private final static Logger logger = LoggerFactory.getLogger(AbstractRetryRegisterService.class);

    private final BlockingQueue<RegisterMeta> retryRegisterMetaQueue = new LinkedBlockingQueue<>();

    public AbstractRetryRegisterService() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    RegisterMeta registerMeta = null;
                    try {
                        registerMeta = retryRegisterMetaQueue.take();
                        doRegister(registerMeta);
                    } catch (InterruptedException e) {
                        logger.warn("[register-thread] Interrupted");
                    } catch (Throwable t) {
                        if (registerMeta != null) {
                            logger.error("Register [{}] fail: {}, will try again...", registerMeta.getServiceMeta(), t);
                            retryRegisterMetaQueue.add(registerMeta);
                        }
                    }
                }
            }
        });
        thread.setName("register-thread");
        thread.start();
    }

    public void retryRegister(RegisterMeta registerMeta) {
        retryRegisterMetaQueue.add(registerMeta);
    }

    protected abstract void doRegister(RegisterMeta registerMeta);

    protected abstract void doUnRegister(RegisterMeta registerMeta);

    protected abstract void doSubscribe(SubscribeMeta serviceMeta);

    protected abstract void doSubscribeGroup();

}
