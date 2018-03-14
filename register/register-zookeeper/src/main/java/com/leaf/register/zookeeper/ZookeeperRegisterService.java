package com.leaf.register.zookeeper;

import com.leaf.common.UnresolvedAddress;
import com.leaf.common.concurrent.ConcurrentSet;
import com.leaf.common.constants.Constants;
import com.leaf.common.model.ServiceMeta;
import com.leaf.common.utils.Maps;
import com.leaf.register.api.AbstractRegisterService;
import com.leaf.register.api.NotifyEvent;
import com.leaf.register.api.RegisterType;
import com.leaf.register.api.model.RegisterMeta;
import com.leaf.register.api.model.SubscribeMeta;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperRegisterService extends AbstractRegisterService {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegisterService.class);

    private static final int CONNECT_TIMEOUT = 3000;

    private static final int SESSION_TIMEOUT = 3000;

    private final ConcurrentMap<SubscribeMeta, PathChildrenCache> pathChildrenCache = Maps.newConcurrentMap();

    /**
     * 指定地址上注册的服务，当注册的服务为空时，通知下线（客户端断开与原有服务端的连接）
     */
    private final ConcurrentMap<UnresolvedAddress, ConcurrentSet<RegisterMeta>> addressRegisters = Maps.newConcurrentMap();

    private CuratorFramework curatorFramework;

    public ZookeeperRegisterService() {
    }

    @Override
    public RegisterType registerType() {
        return RegisterType.ZOOKEEPER;
    }

    @Override
    public void connectToRegistryServer(String addresses) {
        curatorFramework = CuratorFrameworkFactory
                .builder()
                .namespace(Constants.ZOOKEEPER_NAME_SPACE)
                .connectString(addresses)
                .connectionTimeoutMs(CONNECT_TIMEOUT)
                .sessionTimeoutMs(SESSION_TIMEOUT)
                .retryPolicy(new ExponentialBackoffRetry(500, 20))
                .build();

        curatorFramework.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                switch (connectionState) {
                    case RECONNECTED: {
                        // 断线重连
                        for (SubscribeMeta subscribeMeta : getConsumersServiceMetas()) {
                            doSubscribe(subscribeMeta);
                        }
                        for (RegisterMeta registerMeta : getProviderRegisterMetas()) {
                            doRegister(registerMeta);
                        }
                    }
                    default: {
                        logger.info("Zookeeper connection state changed {}.", connectionState);
                    }
                }
            }
        });

        curatorFramework.start();
    }

    @Override
    protected void doRegister(RegisterMeta registerMeta) {
        String directory = String.format("/providers/%s/%s/%s",
                registerMeta.getServiceMeta().getGroup(),
                registerMeta.getServiceMeta().getServiceProviderName(),
                registerMeta.getServiceMeta().getVersion()
        );
        try {
            if (curatorFramework.checkExists().forPath(directory) == null) {
                curatorFramework.create().creatingParentsIfNeeded().forPath(directory);
            }
        } catch (Exception e) {
            logger.warn("create parent node fail directory: {}", directory, e);
        }

        try {
            curatorFramework.create().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
                @Override
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                    int resultCode = curatorEvent.getResultCode();
                    logger.info("zookeeper do register registerMeta: {} result: {}", registerMeta, resultCode);
                    if (KeeperException.Code.OK.intValue() == resultCode) {
                        ZookeeperRegisterService.super.getProviderRegisterMetas().add(registerMeta);
                    } else {
                        ZookeeperRegisterService.super.retryRegister(registerMeta);
                    }
                }
            }).forPath(String.format("%s/%s&%s&%s",
                    directory,
                    String.valueOf(registerMeta.getAddress()),
                    String.valueOf(registerMeta.getWeight()),
                    String.valueOf(registerMeta.getConnCount())
            ));
        } catch (Exception e) {
            logger.error("create register meta mode fail: {}", registerMeta.toString(), e);
        }
    }

    @Override
    protected void doUnRegister(RegisterMeta registerMeta) {
        String directory = String.format("/providers/%s/%s/%s",
                registerMeta.getServiceMeta().getGroup(),
                registerMeta.getServiceMeta().getServiceProviderName(),
                registerMeta.getServiceMeta().getVersion()
        );
        try {
            if (curatorFramework.checkExists().forPath(directory) == null) {
                return;
            }
        } catch (Exception e) {
            logger.warn("do unregister checkExists directory: {} fail", directory, e);
        }

        try {
            curatorFramework.delete().inBackground(new BackgroundCallback() {
                @Override
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                    logger.info("zookeeper do unregister registerMeta: {} result: {}", registerMeta, curatorEvent.getResultCode());
                }
            }).forPath(String.format("%s/%s&%s&%s",
                    directory,
                    String.valueOf(registerMeta.getAddress()),
                    String.valueOf(registerMeta.getWeight()),
                    String.valueOf(registerMeta.getConnCount())
            ));
        } catch (Exception e) {
            logger.warn("create register meta mode fail: {}", registerMeta.toString(), e);
        }
    }

    @Override
    protected void doSubscribe(final SubscribeMeta subscribeMeta) {
        String directory = String.format("/consumers/%s/%s/%s",
                subscribeMeta.getServiceMeta().getGroup(),
                subscribeMeta.getServiceMeta().getServiceProviderName(),
                subscribeMeta.getServiceMeta().getVersion()
        );
        try {
            if (curatorFramework.checkExists().forPath(directory) == null) {
                curatorFramework.create().creatingParentsIfNeeded().forPath(directory);
            }
        } catch (Exception e) {
            logger.warn("create parent node fail directory: {}", directory, e);
        }

        try {
            curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(
                    String.format("%s/%s",
                            directory,
                            subscribeMeta.getAddressHost())
            );
        } catch (Exception e) {
            logger.warn("create subscribe meta mode fail: {}", subscribeMeta.toString(), e);
        }

        PathChildrenCache pathChildrenCache = this.pathChildrenCache.get(subscribeMeta);

        if (pathChildrenCache == null) {
            String directoryProviders = String.format("/providers/%s/%s/%s",
                    subscribeMeta.getServiceMeta().getGroup(),
                    subscribeMeta.getServiceMeta().getServiceProviderName(),
                    subscribeMeta.getServiceMeta().getVersion()
            );
            PathChildrenCache newChildrenCache = new PathChildrenCache(curatorFramework, directoryProviders, false);
            // 添加一次监听
            pathChildrenCache = this.pathChildrenCache.putIfAbsent(subscribeMeta, newChildrenCache);
            if (pathChildrenCache == null) {
                pathChildrenCache = newChildrenCache;
                pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                        PathChildrenCacheEvent.Type type = event.getType();
                        switch (type) {
                            case CHILD_ADDED: {
                                RegisterMeta registerMeta = parseProviderPath(event.getData().getPath());

                                ConcurrentSet<RegisterMeta> registerMetas = getRegisterMetas(registerMeta);
                                registerMetas.add(registerMeta);

                                ZookeeperRegisterService.super.notify(subscribeMeta.getServiceMeta(), NotifyEvent.ADD, registerMeta);
                                break;
                            }
                            case CHILD_REMOVED: {
                                RegisterMeta registerMeta = parseProviderPath(event.getData().getPath());

                                ConcurrentSet<RegisterMeta> registerMetas = getRegisterMetas(registerMeta);
                                registerMetas.remove(registerMeta);

                                if (registerMetas.size() == 0) {
                                    logger.info("[OFFLINE_SERVICE] server: {} offline", registerMeta.getAddress());

                                    ZookeeperRegisterService.super.offline(registerMeta.getAddress());
                                }

                                ZookeeperRegisterService.super.notify(subscribeMeta.getServiceMeta(), NotifyEvent.REMOVE, registerMeta);
                                break;
                            }
                        }
                    }
                });
                try {
                    pathChildrenCache.start();
                } catch (Exception e) {
                    logger.error("zookeeper doSubscribe fail service meta: {}", subscribeMeta, e);
                }
            } else {
                try {
                    newChildrenCache.close();
                } catch (IOException e) {
                    logger.warn("newChildrenCache close fail", e);
                }
            }
        }
    }

    private ConcurrentSet<RegisterMeta> getRegisterMetas(RegisterMeta registerMeta) {
        ConcurrentSet<RegisterMeta> registerMetas = addressRegisters.get(registerMeta.getAddress());
        if (registerMetas == null) {
            ConcurrentSet<RegisterMeta> newRegisterMetas = new ConcurrentSet<>();
            registerMetas = addressRegisters.putIfAbsent(registerMeta.getAddress(), newRegisterMetas);
            if (registerMetas == null) {
                registerMetas = newRegisterMetas;
            }
        }
        return registerMetas;
    }

    /**
     * +-------------------------------------------------------------------------------+
     * | |/leaf(namespace)                                                                        |
     * | |  +                                                                          |
     * | +----> /providers                                                             |
     * | |      +                                                                      |
     * | +---------------> /group                                                      |
     * | |                  +                                                          |
     * | +----------------------> /com.xx.xxService                                    |
     * | |                               +                                             |
     * | +----------------------------------------> /1.0.0                             |
     * | |                                            +                                |
     * | +-------------------------------------------------> /ip:port&weight&connCount |
     * +-------------------------------------------------------------------------------+
     *
     * @param path
     * @return
     */
    private RegisterMeta parseProviderPath(String path) {

        RegisterMeta registerMeta = new RegisterMeta();

        String[] strings0 = path.split("/");

        ServiceMeta serviceMeta = new ServiceMeta(strings0[2], strings0[3], strings0[4]);
        registerMeta.setServiceMeta(serviceMeta);

        String[] strings1 = strings0[5].split("&");
        String[] address = strings1[0].split(":");

        UnresolvedAddress unresolvedAddress = new UnresolvedAddress(address[0], Integer.valueOf(address[1]));

        registerMeta.setAddress(unresolvedAddress);
        registerMeta.setWeight(Integer.valueOf(strings1[1]));
        registerMeta.setConnCount(Integer.valueOf(strings1[2]));
        return registerMeta;
    }
}
