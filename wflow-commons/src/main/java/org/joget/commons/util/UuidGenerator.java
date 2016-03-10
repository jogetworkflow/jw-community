package org.joget.commons.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * Utility methods to generate an UUID
 * 
 */
public class UuidGenerator {

    public static UuidGenerator uuidGenerator;
    private Random seeder;
    private String midValue;
    private char zero[] = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0'
    };

    /**
     * Get the UuidGenerator instance
     * @return 
     */
    public static UuidGenerator getInstance() {
        if (uuidGenerator == null) {
            uuidGenerator = new UuidGenerator();
        }
        return uuidGenerator;
    }

    private UuidGenerator() {
        InetAddress inet = null;
        byte bytes[];
        try {
            inet = InetAddress.getLocalHost();
            bytes = inet.getAddress();
        } catch (UnknownHostException e) {
            bytes = "127.0.0.1".getBytes();
        }
        StringBuffer sb = new StringBuffer();
        for (int c = 0; c < 4; c++) {
            int i = bytes[c] & 0xff;
            sb.append(hexFormat(i, 2));
        }

        String hexInetAddress = sb.toString();
        String thisHashCode = hexFormat(System.identityHashCode(this), 8);
        midValue = hexInetAddress + "-" + thisHashCode;
        seeder = new Random();
    }

    /**
     * Generate an UUID
     * @return 
     */
    public synchronized String getUuid() {
        long timeNow = System.currentTimeMillis();
        int timeLow = (int) timeNow & -1;
        int node = seeder.nextInt();
        return hexFormat(timeLow, 8) + "-" + midValue + "-" + hexFormat(node, 8);
    }

    private String hexFormat(int val, int length) {
        StringBuffer sb = new StringBuffer(Integer.toHexString(val));
        if (sb.length() < length) {
            sb.append(zero, 0, length - sb.length());
        }
        return sb.toString();
    }
}
