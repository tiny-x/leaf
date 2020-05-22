package com.leaf.register.zookeeper;

import com.google.common.collect.Lists;
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
import joptsimple.internal.Strings;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yefei
 */
public class ZookeeperRegisterService extends AbstractRegisterService {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegisterService.class);

    private static final int CONNECT_TIMEOUT = 10000;

    private static final int SESSION_TIMEOUT = 20000;

    private final ConcurrentMap<SubscribeMeta, PathChildrenCache> pathChildrenCache = Maps.newConcurrentMap();

    private final ConcurrentMap<ServiceMeta, PathChildrenCache> serviceMetaPathChildrenCache = Maps.newConcurrentMap();

    /**
     * 记录订注册者信息的版本
     */
    private final ConcurrentMap<RegisterMeta, Long> subscribeProviderVersion = Maps.newConcurrentMap();

    /**
     * 正在注册的服务
     */
    private final ConcurrentSet<RegisterMeta> preProviderRegisterMetas = new ConcurrentSet<>();

    /**
     * 记录订阅消费者信息的版本
     */
    private final ConcurrentHashMap<SubscribeMeta, Long> subscribeConsumerVersion = new ConcurrentHashMap<>();

    private CuratorFramework curatorFramework;

    private String namespace = Constants.ZOOKEEPER_NAME_SPACE;

    public ZookeeperRegisterService() {

    }

    @Override
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public RegisterType registerType() {
        return RegisterType.ZOOKEEPER;
    }

    @Override
    public void connectToRegistryServer(String addresses) {
        curatorFramework = CuratorFrameworkFactory
                .builder()
                .namespace(namespace)
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
                        for (SubscribeMeta subscribeMeta : subscribes) {
                            doSubscribeRegisterMeta(subscribeMeta);
                        }
                        for (RegisterMeta registerMeta : registers) {
                            doRegister(registerMeta);
                        }
                        for (ServiceMeta subscribeConsumer : subscribeConsumers) {
                            doSubscribeSubscribeMeta(subscribeConsumer);
                        }
                    }
                    default: {
                        logger.info("Zookeeper connection state changed {}.", connectionState);
                    }
                }
            }
        });

        curatorFramework.start();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("check pre register metas");
            return thread;
        });

        executor.scheduleAtFixedRate(() -> {
            for (RegisterMeta registerMeta : preProviderRegisterMetas) {
                String directory = String.format("/providers/%s/%s/%s",
                        registerMeta.getServiceMeta().getGroup(),
                        registerMeta.getServiceMeta().getServiceProviderName(),
                        registerMeta.getServiceMeta().getVersion()
                );
                String nodePath = String.format("%s/%s&%s&%s&%s",
                        directory,
                        registerMeta.getAddress(),
                        registerMeta.getWeight(),
                        registerMeta.getConnCount(),
                        Arrays.toString(registerMeta.getMethods()).substring(1, Arrays.toString(registerMeta.getMethods()).length() - 1)
                );
                try {
                    if (curatorFramework.checkExists().forPath(nodePath) == null) {
                        doRegister(registerMeta);
                    }
                } catch (Exception e) {
                    logger.warn("check path exists fail: {}, e: {}", directory, e.getMessage());
                }

            }
        }, 500, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doRegister(RegisterMeta registerMeta) {
        preProviderRegisterMetas.add(registerMeta);

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
            logger.warn("create parent node fail directory: {}, e: {}", directory, e.getMessage());
        }

        try {
            curatorFramework.create().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
                @Override
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                    int resultCode = curatorEvent.getResultCode();
                    logger.info("zookeeper do register registerMeta: {} result: {}", registerMeta, resultCode);
                    if (KeeperException.Code.OK.intValue() == resultCode) {
                        preProviderRegisterMetas.remove(registerMeta);
                        registers.add(registerMeta);
                    }
                }
            }).forPath(String.format("%s/%s&%s&%s&%s",
                    directory,
                    registerMeta.getAddress(),
                    registerMeta.getWeight(),
                    registerMeta.getConnCount(),
                    Arrays.toString(registerMeta.getMethods()).substring(1, Arrays.toString(registerMeta.getMethods()).length() - 1)
            ));
        } catch (Exception e) {
            logger.error("create register meta mode fail: {}, e: {}", registerMeta.toString(), e.getMessage());
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
            logger.warn("do unregister checkExists directory: {} fail, e: {}", directory, e.getMessage());
        }

        try {
            curatorFramework.delete().inBackground(new BackgroundCallback() {
                @Override
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                    logger.info("zookeeper do unregister registerMeta: {} result: {}", registerMeta, curatorEvent.getResultCode());
                }
            }).forPath(String.format("%s/%s&%s&%s&%s",
                    directory,
                    registerMeta.getAddress(),
                    registerMeta.getWeight(),
                    registerMeta.getConnCount(),
                    Arrays.toString(registerMeta.getMethods()).substring(1, Arrays.toString(registerMeta.getMethods()).length() - 1)
            ));
        } catch (Exception e) {
            logger.warn("create register meta mode fail: {}, e: {}", registerMeta.toString(), e.getMessage());
        }
    }

    @Override
    protected void doSubscribeRegisterMeta(final SubscribeMeta subscribeMeta) {
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
            logger.warn("create parent node fail directory: {}, e: {}", directory, e.getMessage());
        }

        //  如果是控制台订阅的 不需要创建 订阅节点
        if (!Strings.isNullOrEmpty(subscribeMeta.getAddressHost())) {
            try {
                curatorFramework.create().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
                    @Override
                    public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                        logger.info("zookeeper do subscribe registerMeta: {} result: {}", subscribeMeta, curatorEvent.getResultCode());
                    }
                }).forPath(
                        String.format("%s/%s",
                                directory,
                                subscribeMeta.getAddressHost())
                );
            } catch (Exception e) {
                logger.warn("create subscribe meta mode fail: {}, e: {}", subscribeMeta.toString(), e.getMessage());
            }
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
                                if (registerMeta != null) {
                                    subscribeProviderVersion.put(registerMeta, event.getData().getStat().getCzxid());
                                    registerMeta.setServiceMeta(subscribeMeta.getServiceMeta());
                                    ZookeeperRegisterService.super.notify(NotifyEvent.ADD, registerMeta);
                                }
                                break;
                            }
                            case CHILD_REMOVED: {
                                RegisterMeta registerMeta = parseProviderPath(event.getData().getPath());
                                if (registerMeta != null) {
                                    Long originZxId = subscribeProviderVersion.get(registerMeta);
                                    if (originZxId == null) {
                                        //TODO
                                        break;
                                    }

                                    Long curZxId = event.getData().getStat().getCzxid();
                                    if (originZxId > curZxId) {
                                        logger.warn("cur zxId {}, zxId: {}  cur zxId > zxId, so ignore CHILD_REMOVED, registerMeta:{}",
                                                curZxId,
                                                originZxId,
                                                registerMeta);
                                        break;
                                    }
                                    registerMeta.setServiceMeta(subscribeMeta.getServiceMeta());
                                    ZookeeperRegisterService.super.notify(NotifyEvent.REMOVE, registerMeta);

                                    synchronized (registerMeta.getAddress().toString()) {
                                        subscribeProviderVersion.remove(registerMeta);
                                        boolean offline = true;
                                        for (Map.Entry<RegisterMeta, Long> entry : subscribeProviderVersion.entrySet()) {
                                            if (offline && entry.getKey().getAddress().equals(registerMeta.getAddress())) {
                                                offline = false;
                                                break;
                                            }
                                        }
                                        if (offline) {
                                            ZookeeperRegisterService.super.offline(registerMeta.getAddress());
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                            default:
                                //ignore
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

    @Override
    protected void doSubscribeSubscribeMeta(ServiceMeta serviceMeta) {

        PathChildrenCache pathChildrenCache = serviceMetaPathChildrenCache.get(serviceMeta);

        if (pathChildrenCache == null) {
            String directory = String.format("/consumers/%s/%s/%s",
                    serviceMeta.getGroup(),
                    serviceMeta.getServiceProviderName(),
                    serviceMeta.getVersion()
            );
            PathChildrenCache newChildrenCache = new PathChildrenCache(curatorFramework, directory, false);
            // 添加一次监听
            pathChildrenCache = serviceMetaPathChildrenCache.putIfAbsent(serviceMeta, newChildrenCache);
            if (pathChildrenCache == null) {
                pathChildrenCache = newChildrenCache;
                pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                        PathChildrenCacheEvent.Type type = event.getType();
                        switch (type) {
                            case CHILD_ADDED: {
                                SubscribeMeta subscribeMeta = parseConsumerPath(event.getData().getPath());
                                if (serviceMeta != null) {
                                    subscribeMeta.setServiceMeta(serviceMeta);
                                    subscribeConsumerVersion.put(subscribeMeta, event.getData().getStat().getCzxid());
                                    ZookeeperRegisterService.super.notify(NotifyEvent.ADD, subscribeMeta);
                                }
                                break;
                            }
                            case CHILD_REMOVED: {
                                SubscribeMeta subscribeMeta = parseConsumerPath(event.getData().getPath());
                                if (subscribeMeta != null) {
                                    Long zxId = subscribeConsumerVersion.get(subscribeMeta);
                                    if (zxId == null) {
                                        break;
                                    }
                                    Long curZxId = event.getData().getStat().getCzxid();
                                    if (zxId > curZxId) {
                                        logger.warn("cur zxId {}, zxId: {}  cur zxId > zxId, so ignore CHILD_REMOVED, serviceMeta:{}",
                                                curZxId,
                                                zxId,
                                                serviceMeta);
                                        break;
                                    }
                                    subscribeConsumerVersion.remove(serviceMeta);
                                    subscribeMeta.setServiceMeta(serviceMeta);
                                    ZookeeperRegisterService.super.notify(NotifyEvent.REMOVE, subscribeMeta);
                                }
                            }
                            default:
                                //ignore
                        }
                    }
                });
                try {
                    pathChildrenCache.start();
                } catch (Exception e) {
                    logger.error("zookeeper doSubscribe fail service meta: {}", serviceMeta, e);
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

    @Override
    public List<ServiceMeta> lookup() {
        List<ServiceMeta> serviceMetas = Lists.newArrayList();
        try {
            List<String> groups = curatorFramework.getChildren().forPath("/consumers");
            for (String group : groups) {
                List<String> services = curatorFramework.getChildren().forPath("/consumers/" + group);
                for (String service : services) {
                    List<String> versions = curatorFramework.getChildren().forPath("/consumers/" + group + "/" + service);
                    for (String version : versions) {
                        ServiceMeta serviceMeta = new ServiceMeta(group, service, version);
                        serviceMetas.add(serviceMeta);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("lookup service fail", e);
        }
        return serviceMetas;
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
     * | +-------------------------------------------------> /ip:port&weight&connCount&m1,m2 |
     * +-------------------------------------------------------------------------------+
     *
     * @param path
     * @return
     */
    private RegisterMeta parseProviderPath(String path) {
        String[] strings0 = path.split("/");
        if (strings0.length == 6) {
            RegisterMeta registerMeta = new RegisterMeta();
            ServiceMeta serviceMeta = new ServiceMeta(strings0[2], strings0[3], strings0[4]);
            registerMeta.setServiceMeta(serviceMeta);


            String[] strings1 = strings0[5].split("&");
            String[] address = strings1[0].split(":");

            UnresolvedAddress unresolvedAddress = new UnresolvedAddress(address[0], Integer.valueOf(address[1]));

            registerMeta.setAddress(unresolvedAddress);
            registerMeta.setWeight(Integer.valueOf(strings1[1]));
            registerMeta.setConnCount(Integer.valueOf(strings1[2]));
            registerMeta.setMethods(strings1[3].split(","));
            return registerMeta;
        } else {
            return null;
        }
    }

    private SubscribeMeta parseConsumerPath(String path) {
        String[] strings0 = path.split("/");
        if (strings0.length >= 5) {
            SubscribeMeta subscribeMeta = new SubscribeMeta();
            ServiceMeta serviceMeta = new ServiceMeta(strings0[2], strings0[3], strings0[4]);
            subscribeMeta.setServiceMeta(serviceMeta);
            if (strings0.length == 6) {
                subscribeMeta.setAddressHost(strings0[5]);
            }
            return subscribeMeta;
        } else {
            return null;
        }
    }
}
