package javax.rmi;

import java.rmi.Remote;

/**
 * Dummy class to cater for Java 11
 */
public class PortableRemoteObject {
    
    public static void unexportObject(Remote obj) {
    }
    
}
