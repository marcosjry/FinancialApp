package com.marcos.orchestrator.service.domain.config.infra;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

@Component
public class ConnectionPool {
    private final BlockingQueue<Channel> channels;
    private final ConnectionFactory factory;
    private final int poolSize;

    private Connection connection;

    public ConnectionPool(@Value("${spring.rabbitmq.pool.size:10}") int poolSize) {
        this.poolSize = poolSize;
        this.channels = new ArrayBlockingQueue<>(poolSize);
        this.factory = createConnectionFactory();
        try {
            initializePool();
        } catch (Exception e) {
            System.err.println("Erro ao inicializar pool: " + e.getMessage());
        }
    }

    private ConnectionFactory createConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbitmq"); // localhost
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(10000);
        factory.setConnectionTimeout(5000);
        factory.setRequestedHeartbeat(30);
        return factory;
    }

    private void initializePool() throws IOException, TimeoutException {
        if (connection == null || !connection.isOpen()) {
            connection = factory.newConnection();
        }

        channels.clear();

        for (int i = 0; i < poolSize; i++) {
            try {
                Channel channel = createNewChannel();
                channels.offer(channel);
            } catch (Exception e) {
                System.err.println("Erro ao criar canal " + i + ": " + e.getMessage());
            }
        }
    }

    private Channel createNewChannel() throws IOException, TimeoutException {
        if (connection == null || !connection.isOpen()) {
            connection = factory.newConnection();
        }
        Channel channel = connection.createChannel();
        channel.basicQos(5);  // Define prefetch para balancear carga
        return channel;
    }

    public Channel acquireChannel() {
        Channel channel = channels.poll();
        try {
            if (channel == null || !channel.isOpen()) {
                return createNewChannel();
            }
            return channel;
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao adquirir canal", e);
        }
    }

    public void releaseChannel(Channel channel) {
        if (channel != null) {
            try {
                if (channel.isOpen()) {
                    channels.offer(channel);
                } else {
                    channels.offer(createNewChannel());
                }
            } catch (Exception e) {
                System.err.println("Erro ao liberar canal: " + e.getMessage());
                try {
                    if (channel.isOpen()) {
                        channel.close();
                    }
                } catch (Exception ex) {
                    System.err.println("Erro ao fechar canal problemático: " + ex.getMessage());
                }
            }
        }
    }
}
