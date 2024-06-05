package org.joget.commons.ignite;

import java.util.Arrays;
import java.util.Collection;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicyFactory;
import org.apache.ignite.cluster.ClusterState;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.NearCacheConfiguration;
import org.apache.ignite.kubernetes.configuration.KubernetesConnectionConfiguration;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.kubernetes.TcpDiscoveryKubernetesIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.query.Query;
import org.joget.commons.util.HostManager;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.SetupManager;

/**
 * To initialize and manage the Apache Ignite cache.
 * 
 */
public class IgniteCacheManager {
    
    public static final String SYSTEM_PROPERTY_IGNITE_CACHE = "wflow.ignite";
    public static final String SYSTEM_PROPERTY_IGNITE_K8S_NAMESPACE = "wflow.igniteK8sNamespace";
    public static final String SYSTEM_PROPERTY_IGNITE_K8S_SERVICE = "wflow.igniteK8sService";
    public static final String SYSTEM_PROPERTY_IGNITE_STATIC_IP = "wflow.igniteStaticIp";
    public static final String SYSTEM_PROPERTY_IGNITE_REPLICATED = "wflow.igniteReplicated";
    public static final String SYSTEM_PROPERTY_IGNITE_MAX_ASYNC = "wflow.igniteMaxAsync";
    public static final String SYSTEM_PROPERTY_IGNITE_THREAD_POOL_SIZE = "wflow.igniteThreadPoolSize";
    public static final String SYSTEM_PROPERTY_IGNITE_PARTITIONS = "wflow.ignitePartitions";
    public static final String SYSTEM_PROPERTY_IGNITE_PARTITION_BACKUPS = "wflow.ignitePartitionBackups";
    public static final String SYSTEM_PROPERTY_IGNITE_PAIRED_CONNECTIONS = "wflow.ignitePairedConnections";
    public static final String SYSTEM_PROPERTY_IGNITE_NODE_CONNECTIONS = "wflow.igniteNodeConnections";
    public static final String SYSTEM_PROPERTY_IGNITE_NEAR_CACHE = "wflow.igniteNearCache";
    private static boolean started = false;
    static IgniteConfiguration igniteCfg;

    public IgniteCacheManager() {
        // default constructor        
    }
    
    public IgniteCacheManager(IgniteConfiguration igniteCfg) {
        initIgniteCache(igniteCfg);
    }

    /**
     * Initialize the Ignite cache based on the IgniteConfiguration.
     * If within a Kubernetes environment and system properties 
     * SYSTEM_PROPERTY_IGNITE_K8S_NAMESPACE and SYSTEM_PROPERTY_IGNITE_K8S_SERVICE are defined, 
     * will run as a client node and KubernetesConfiguration will be used.
     * If the system property SYSTEM_PROPERTY_IGNITE_STATIC_IP is defined, 
     * will run as a client node static IP discovery will be used.
     * Otherwise, it will run embedded as a server node.
     * @param igniteCfg 
     */
    public static void initIgniteCache(IgniteConfiguration igniteCfg) {
        if (!HostManager.isVirtualHostEnabled() && IgniteCacheManager.isIgniteCacheEnabled()) {
            // set ignite work directory
            String workDirectory = SetupManager.getBaseDirectory() + "/ignite/work";
            igniteCfg.setWorkDirectory(workDirectory);
            
            // detect environment
            boolean isKubernetesEnv = System.getenv("KUBERNETES_SERVICE_HOST") != null;
            String k8sNamespace = System.getProperty(SYSTEM_PROPERTY_IGNITE_K8S_NAMESPACE);
            String k8sService = System.getProperty(SYSTEM_PROPERTY_IGNITE_K8S_SERVICE);
            String staticIp = System.getProperty(SYSTEM_PROPERTY_IGNITE_STATIC_IP);
            if (isKubernetesEnv && k8sNamespace != null && k8sService != null) {
                // get k8s namespace and service name
                KubernetesConnectionConfiguration k8sConfig = new KubernetesConnectionConfiguration();
                k8sConfig.setNamespace(k8sNamespace);
                k8sConfig.setServiceName(k8sService);

                // set k8s ip finder
                TcpDiscoveryKubernetesIpFinder k8sIpFinder = new TcpDiscoveryKubernetesIpFinder(k8sConfig);
                TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
                discoverySpi.setIpFinder(k8sIpFinder);
                igniteCfg.setDiscoverySpi(discoverySpi);

                // set client mode
                igniteCfg.setClientMode(true);
                Ignition.setClientMode(true);
                LogUtil.info(IgniteCacheManager.class.getName(), "Running Ignite client node to cluster in Kubernetes namespace " + k8sNamespace + ", service name " + k8sService);
            } else if (staticIp != null) {
                // use static ip
                Collection<String> addresses = Arrays.asList(staticIp.split(",", -1));
                TcpDiscoveryVmIpFinder staticIpFinder = new TcpDiscoveryVmIpFinder();
                staticIpFinder.setAddresses(addresses);
                TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();
                discoverySpi.setIpFinder(staticIpFinder);
                igniteCfg.setDiscoverySpi(discoverySpi);

                // set client mode
                igniteCfg.setClientMode(true);
                Ignition.setClientMode(true);
                LogUtil.info(IgniteCacheManager.class.getName(), "Running Ignite client node to cluster using static IP " + staticIp);
            } else {
                // use embedded mode
                Ignition.setClientMode(false);
                LogUtil.info(IgniteCacheManager.class.getName(), "Running Ignite server node in embedded mode");
            }

            // set thread pool size
            String threadPoolSizeStr = System.getProperty(SYSTEM_PROPERTY_IGNITE_THREAD_POOL_SIZE);
            if (threadPoolSizeStr != null) {
                try {
                    int threadPoolSize = Integer.parseInt(threadPoolSizeStr);
                    if (threadPoolSize > 0) {
                        igniteCfg.setSystemThreadPoolSize(threadPoolSize);
                        igniteCfg.setStripedPoolSize(threadPoolSize);
                        igniteCfg.setPublicThreadPoolSize(threadPoolSize);
                        LogUtil.info(IgniteCacheManager.class.getName(), "Using Ignite thread pool size " + threadPoolSize);
                    }
                } catch(NumberFormatException e) {
                    // ignore
                }
            }            
            
            // set custom connection settings            
            boolean usePairedConnections = Boolean.parseBoolean(System.getProperty(SYSTEM_PROPERTY_IGNITE_PAIRED_CONNECTIONS));
            String nodeConnectionsStr = System.getProperty(SYSTEM_PROPERTY_IGNITE_NODE_CONNECTIONS);
            if (usePairedConnections || nodeConnectionsStr != null) {
                TcpCommunicationSpi commSpi = new TcpCommunicationSpi();
                if (usePairedConnections) {
                    commSpi.setUsePairedConnections(true);
                    LogUtil.info(IgniteCacheManager.class.getName(), "Using Ignite paired connections");
                }
                int nodeConnections = 1;
                try {
                    nodeConnections = Integer.parseInt(nodeConnectionsStr);
                } catch(NumberFormatException e) {
                    // ignore
                }
                commSpi.setConnectionsPerNode(nodeConnections);    
                
                // set timeouts
                commSpi.setConnectTimeout(30000);
                commSpi.setMaxConnectTimeout(60000);
                commSpi.setSocketWriteTimeout(10000);                
                igniteCfg.setCommunicationSpi(commSpi);
                LogUtil.info(IgniteCacheManager.class.getName(), "Using Ignite with " + nodeConnections + " connections per node");
            }
            IgniteCacheManager.igniteCfg = igniteCfg;            
            
            // start cluster
            Ignite ignite = Ignition.getOrStart(igniteCfg);
            ignite.cluster().state(ClusterState.ACTIVE);
            started = true;
        }
    }
    
    /**
     * Checks to see whether the Ignite cache is enabled.
     * @return true if the cache is enabled.
     */
    public static boolean isIgniteCacheEnabled() {
        boolean enabled = Boolean.parseBoolean(System.getProperty(SYSTEM_PROPERTY_IGNITE_CACHE));
        return enabled;
    }
    
    /**
     * Checks to see whether the Ignite cache is started.
     * @return true if the cache is started.
     */
    public static boolean isStarted() {
        return started;
    }
    
    /**
     * Obtain reference to the Ignite cache instance.
     * @return 
     */
    public static Ignite getIgnite() {
        Ignite ignite = null;
        if (started) {
            ignite = Ignition.getOrStart(igniteCfg);
        }
        return ignite;
    }
    
    /**
     * Clear all caches in the Ignite grid.
     */
    public static void clearAll() {
        if (started) {
            Ignite ignite = Ignition.getOrStart(igniteCfg);
            Collection<String> cacheNames = ignite.cacheNames();
            for (String cacheName: cacheNames) {
                IgniteCache cache = ignite.getOrCreateCache(cacheName);
                cache.clear();
            }
        }
    }

    /**
     * Clear a specific cache in the Ignite grid.
     * @param cacheName 
     */
    public static void clear(String cacheName) {
        if (started) {
            Ignite ignite = Ignition.getOrStart(igniteCfg);
            IgniteCache cache = ignite.getOrCreateCache(cacheName);
            cache.clear();
        }
    }
    
    /**
     * Sets a Hibernate query to be cacheable along with an optional custom region name.
     * @param query
     * @param regionName If no region name is specified, the default "default-query-results-region" query cache name is used.
     * @return 
     */
    public static Query setCacheable(Query query, String regionName) {
        String cacheRegionName = (regionName != null && !regionName.isEmpty()) ? "query.cache." + regionName : RegionFactory.DEFAULT_QUERY_RESULTS_REGION_UNQUALIFIED_NAME;
        query.setCacheable(true);
        query.setCacheRegion(cacheRegionName);
        return query;
    }
    
    /**
     * Sets the Ignite cache mode based on configured system property.
     * @param cacheConfig 
     */
    public static void setCacheMode(CacheConfiguration cacheConfig) {
        // set cache mode i.e. PARTITIONED or REPLICATED
        boolean useReplicatedCacheMode = Boolean.parseBoolean(System.getProperty(SYSTEM_PROPERTY_IGNITE_REPLICATED));
        if (useReplicatedCacheMode) {
            cacheConfig.setCacheMode(CacheMode.REPLICATED);
        }
        
        // set max concurrent asynchronous operations
        int maxAsync = CacheConfiguration.DFLT_MAX_CONCURRENT_ASYNC_OPS;
        String maxAsyncStr = System.getProperty(SYSTEM_PROPERTY_IGNITE_MAX_ASYNC);
        if (maxAsyncStr != null) {
            try {
                maxAsync = Integer.parseInt(maxAsyncStr);
            } catch(NumberFormatException e) {
                // ignore
            }
        }
        cacheConfig.setMaxConcurrentAsyncOperations(maxAsync);
        
        // set number of partitions
        String partitionStr = System.getProperty(SYSTEM_PROPERTY_IGNITE_PARTITIONS);
        if (partitionStr != null) {
            try {
                int partitions = Integer.parseInt(partitionStr);
                if (partitions > 0) {
                    RendezvousAffinityFunction affinityFunction = new RendezvousAffinityFunction();
                    affinityFunction.setPartitions(partitions);
                    cacheConfig.setAffinity(affinityFunction);
                    LogUtil.debug(IgniteCacheManager.class.getName(), "Using Ignite RendezvousAffinityFunction with " + partitions + " partitions for cache " + cacheConfig.getName());
                }
            } catch(NumberFormatException e) {
                // ignore
            }
        }
        
        // set partition backups
        String backupsStr = System.getProperty(SYSTEM_PROPERTY_IGNITE_PARTITION_BACKUPS);
        if (backupsStr != null) {
            try {
                int backups = Integer.parseInt(backupsStr);
                if (backups >= 0) {
                    cacheConfig.setBackups(backups);
                    LogUtil.debug(IgniteCacheManager.class.getName(), "Using Ignite partition backups to " + backups + " for cache " + cacheConfig.getName());
                }
            } catch(NumberFormatException e) {
                // ignore
            }
        }

        // enable near cache
        String nearCacheStr = System.getProperty(SYSTEM_PROPERTY_IGNITE_NEAR_CACHE);
        if (nearCacheStr != null) {
            try {
                int nearCacheLimit = Integer.parseInt(nearCacheStr);
                if (nearCacheLimit >= 0) {
                    NearCacheConfiguration<Integer, String> nearCfg = new NearCacheConfiguration<>();
                    nearCfg.setNearEvictionPolicyFactory(new LruEvictionPolicyFactory<>(nearCacheLimit));
                    cacheConfig.setNearConfiguration(nearCfg);
                    LogUtil.debug(IgniteCacheManager.class.getName(), "Using Ignite near cache with " + nearCacheLimit + " limit for cache " + cacheConfig.getName());
                }
            } catch(NumberFormatException e) {
                // ignore
            }
        }
    }

    /**
     * Return the timeout for asynchronous cache gets and puts in milliseconds.
     * Configurable via system property wflow.igniteAsyncTimeout.
     * @return
     */
    public static long getConfigAsyncTimeout() {
        long timeout = 0L;
        String timeoutStr = System.getProperty(IgniteJdbcCacheManager.SYSTEM_PROPERTY_IGNITE_CACHE_ASYNC_TIMEOUT);
        if (timeoutStr != null) {
            try {
                timeout = Long.parseLong(timeoutStr);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return timeout;
    }
    
}
