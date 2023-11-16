package org.joget.commons.util;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;

/**
 * Distributed Time-based ID Generator.
 * Code from callicoder: https://www.callicoder.com/distributed-unique-id-sequence-number-generator/
 * Inspired by Twitter snowflake: https://github.com/twitter/snowflake/tree/snowflake-2010
 *
 * This class should be used as a Singleton.
 * Make sure that you create and reuse a Single instance of SequenceGenerator per node in your distributed system cluster.
 */
public class DistributedIdGenerator {
    private static final int UNUSED_BITS = 1; // Sign bit, Unused (always set to 0)
    private static final int EPOCH_BITS = 41;
    private static final int NODE_ID_BITS = 10;
    private static final int SEQUENCE_BITS = 12;

    private static final int MAX_NODE_ID = (int)(Math.pow(2, NODE_ID_BITS) - 1);
    private static final int MAX_SEQUENCE = (int)(Math.pow(2, SEQUENCE_BITS) - 1);

    // Custom Epoch (January 1, 2015 Midnight UTC = 2015-01-01T00:00:00Z)
    private static final long CUSTOM_EPOCH = 1420070400000L;

    private final int nodeId;

    private volatile long lastTimestamp = -1L;
    private static volatile long sequence = 0L;
    
    private static DistributedIdGenerator INSTANCE;

    // Let SequenceGenerator generate a nodeId
    private DistributedIdGenerator() {
        this.nodeId = createNodeId();
    }
    
    public synchronized static DistributedIdGenerator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DistributedIdGenerator();
        }
        return INSTANCE;
    }

    public synchronized long nextId() {
        // update current time
        long currentTimestamp = timestamp();

        // error if current time is before last saved time
        if (currentTimestamp < lastTimestamp) {
            throw new IllegalStateException("Invalid System Clock!");
        }

        if (currentTimestamp == lastTimestamp) {
            // if sequence reached limit, wait till next millisecond.
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0) {
                currentTimestamp = waitNextMillis(currentTimestamp);
            }
        } else {
            // reset sequence to start with zero for the next millisecond
            sequence = 0;
        }

        lastTimestamp = currentTimestamp;

        long id = currentTimestamp << (NODE_ID_BITS + SEQUENCE_BITS);
        id |= (nodeId << SEQUENCE_BITS);
        id |= sequence;
        return id;
    }


    // Get current timestamp in milliseconds, adjust for the custom epoch.
    private static long timestamp() {
        return Instant.now().toEpochMilli() - CUSTOM_EPOCH;
    }

    // Block and wait till next millisecond
    private long waitNextMillis(long currentTimestamp) {
        while (currentTimestamp == lastTimestamp) {
            currentTimestamp = timestamp();
        }
        return currentTimestamp;
    }

    private int createNodeId() {
        int newNodeId;
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    for(int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X", mac[i]));
                    }
                }
            }
            newNodeId = sb.toString().hashCode();
        } catch (Exception ex) {
            newNodeId = (new SecureRandom().nextInt());
        }
        newNodeId = newNodeId & MAX_NODE_ID;
        return newNodeId;
    }
}
