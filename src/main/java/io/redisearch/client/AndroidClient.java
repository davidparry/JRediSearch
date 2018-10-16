package io.redisearch.client;

import io.redisearch.rdbc.JedisWrapper;
import redis.clients.jedis.Jedis;
import redis.clients.rdbc.Connection;

public class AndroidClient extends BaseClient {
    private String host;
    private int port;
    private Connection connection;

    public AndroidClient(String indexName, String host, int port) {
        super(indexName, new Commands.SingleNodeCommands());
        this.host = host;
        this.port = port;
    }

    @Override
    Connection _conn() {
        close();
        Jedis jedis = new Jedis(this.host, this.port);
        connection = new JedisWrapper(jedis);
        return connection;
    }

    public void close() {
        if (this.connection != null) {
            this.connection.close();
        }
    }
}
