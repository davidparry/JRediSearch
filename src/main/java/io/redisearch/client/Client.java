package io.redisearch.client;

import io.redisearch.rdbc.JedisPoolWrapper;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.rdbc.Connection;
import redis.clients.rdbc.Pool;

import java.util.Set;

/**
 * Client is the main RediSearch client class, wrapping connection management and all RediSearch commands
 */
public class Client extends BaseClient {

    private Pool<Connection> pool;

    /**
     * Create a new client to a RediSearch index
     *
     * @param indexName the name of the index we are connecting to or creating
     * @param host      the redis host
     * @param port      the redis pot
     */
    public Client(String indexName, String host, int port, int timeout, int poolSize) {
        this(indexName, host, port, timeout, poolSize, null);
    }

    public Client(String indexName, String host, int port, int timeout, int poolSize, String password) {
        this(indexName, new JedisPoolWrapper(new JedisPool(initPoolConfig(poolSize), host, port, timeout, password)));
        this.commands = new Commands.SingleNodeCommands();
    }

    public Client(String indexName, String host, int port) {
        this(indexName, host, port, 500, 100);
    }

    /**
     * Create a new client to a RediSearch index with JediSentinelPool implementation. JedisSentinelPool
     * takes care of reconfiguring the Pool when there is a failover of master node thus providing high
     * availability and automatic failover.
     *
     * @param indexName  the name of the index we are connecting to or creating
     * @param masterName the masterName to connect from list of masters monitored by sentinels
     * @param sentinels  the set of sentinels monitoring the cluster
     * @param timeout    the timeout in milliseconds
     * @param poolSize   the poolSize of JedisSentinelPool
     * @param password   the password for authentication in a password protected Redis server
     */
    public Client(String indexName, String masterName, Set<String> sentinels, int timeout, int poolSize, String password) {
        this(indexName, new JedisPoolWrapper(new JedisSentinelPool(masterName, sentinels, initPoolConfig(poolSize), timeout, password)));
        this.commands = new Commands.SingleNodeCommands();
    }

    /**
     * Create a new client to a RediSearch index with JediSentinelPool implementation. JedisSentinelPool
     * takes care of reconfiguring the Pool when there is a failover of master node thus providing high
     * availability and automatic failover.
     *
     * <p>The Client is initialized with following default values for {@link JedisSentinelPool}
     * <ul><li> password - NULL, no authentication required to connect to Redis Server</li></ul>
     *
     * @param indexName  the name of the index we are connecting to or creating
     * @param masterName the masterName to connect from list of masters monitored by sentinels
     * @param sentinels  the set of sentinels monitoring the cluster
     * @param timeout    the timeout in milliseconds
     * @param poolSize   the poolSize of JedisSentinelPool
     */
    public Client(String indexName, String masterName, Set<String> sentinels, int timeout, int poolSize) {
        this(indexName, masterName, sentinels, timeout, poolSize, null);
    }

    /**
     * Create a new client to a RediSearch index with JediSentinelPool implementation. JedisSentinelPool
     * takes care of reconfiguring the Pool when there is a failover of master node thus providing high
     * availability and automatic failover.
     *
     * <p>The Client is initialized with following default values for {@link JedisSentinelPool}
     * <ul> <li>timeout - 500 mills</li>
     * <li> poolSize - 100 connections</li>
     * <li> password - NULL, no authentication required to connect to Redis Server</li></ul>
     *
     * @param indexName  the name of the index we are connecting to or creating
     * @param masterName the masterName to connect from list of masters monitored by sentinels
     * @param sentinels  the set of sentinels monitoring the cluster
     */
    public Client(String indexName, String masterName, Set<String> sentinels) {
        this(indexName, masterName, sentinels, 500, 100);
    }

    /**
     * Create a new client to a RediSearch index with JediSentinelPool implementation that the caller passes in.
     *
     * @param indexName the name of the index we are connecting to or creating
     * @param pool      the pool for Jedis that the caller wants to use
     */
    public Client(String indexName, Pool<Connection> pool) {
        super(indexName, new Commands.SingleNodeCommands());
        this.pool = pool;
    }


    /**
     * Constructs JedisPoolConfig object.
     *
     * @param poolSize size of the JedisPool
     * @return {@link JedisPoolConfig} object with a few default settings
     */
    private static JedisPoolConfig initPoolConfig(int poolSize) {
        JedisPoolConfig conf = new JedisPoolConfig();
        conf.setMaxTotal(poolSize);
        conf.setTestOnBorrow(false);
        conf.setTestOnReturn(false);
        conf.setTestOnCreate(false);
        conf.setTestWhileIdle(false);
        conf.setMinEvictableIdleTimeMillis(60000);
        conf.setTimeBetweenEvictionRunsMillis(30000);
        conf.setNumTestsPerEvictionRun(-1);
        conf.setFairness(true);

        return conf;
    }

    Connection _conn() {
        return pool.getResource();
    }


}
