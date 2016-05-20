/*
 * Frightening License 1.0
 * 
 * This software probably contains trojans and backdoors. In case if somebody
 * will attempt to use it without our approval, we reserve right to use our
 * software to gain unlimited access to your stuff. Feel the fear. Maybe even
 * viewing this code could put your system in danger. Who knowns...
 */
package ns.nativesockets;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Inet6Address;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nuke Sparrow <nukesparrow@bitmessage.ch>
 */
public class NativeSocket implements Closeable {
    
    static {
        StringBuilder libName = new StringBuilder("NativeSocket-");
        
        String arch = System.getProperty("os.arch", "x86").toLowerCase();
        
        if ("amd64".equals(arch)) {
            arch = "x86_64";
        }
        else if ("x86".equals("arch") || "i586".equals("arch") || "i686".equals("arch")) {
            arch = "i386";
        }
        
        libName.append(arch);
        libName.append('-');
        libName.append(System.getProperty("os.name", "linux").toLowerCase());
        libName.append("-gnu");
        
        if (!Util.isUnix()) {
            throw new UnsupportedOperationException("OS not supported");
        }
        
        String libFilename = "lib" + libName + ".so";
        File libFile = new File("/tmp/" + libFilename);
        
        try (InputStream i = NativeSocket.class.getResourceAsStream(libFilename)) {
            if (i == null) {
                throw new UnsupportedOperationException("Missing native bindings for OS ("+ libFilename +")");
            }

            try (FileOutputStream o = new FileOutputStream(libFile)) {
                byte[] buf = new byte[2048];
                
                while (true) {
                    int n = i.read(buf);
                    if (n > 0) {
                        o.write(buf, 0, n);
                    }
                    if (n == -1) {
                        break;
                    }
                }
                
                libFile.deleteOnExit();
            } catch (IOException ex) {
                libFile.delete();
                throw new RuntimeException(ex);
            }

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        try {
            Method loadLibraryMethod = ClassLoader.class.getDeclaredMethod("loadLibrary", Class.class, String.class, Boolean.TYPE);
            
            loadLibraryMethod.setAccessible(true);
            
            loadLibraryMethod.invoke(null, NativeSocket.class, libFile.getAbsolutePath(), true);
        
        } catch (ReflectiveOperationException | RuntimeException ex) {
            Logger.getLogger(NativeSocket.class.getName()).log(Level.SEVERE, null, ex);
            System.loadLibrary(libFile.getAbsolutePath());
        }
    }

    private int fd = -1;

    public NativeSocket() {
    }
    
    private int domain;
    
    public void connect(InetSocketAddress endpoint, int timeout) throws IOException {
        if (fd != -1) {
            throw new IllegalStateException();
        }

        domain = endpoint.getAddress() instanceof Inet6Address ? AF_INET6 : AF_INET;
        fd = socket(domain, SOCK_STREAM, 0);
        
        if (timeout > 0) {
            configureBlocking(fd, false);
        }
        
        try {
            connect(fd, domain, endpoint.getAddress().getAddress(), endpoint.getPort());
            
            return;
        } catch (NativeSocketIOException ex) {
            if (timeout <= 0 || ex.getErrno() != EINPROGRESS) {
                throw ex;
            }
        }
        
        configureBlocking(fd, true);
        
        if ((poll(fd, POLLERR | POLLOUT, timeout) & POLLOUT) != 0) {
            return;
        }
        
        close(fd);
        fd = -1;
        throw new IOException("Connection failed");
    }

    public void connect(InetSocketAddress endpoint) throws IOException {
        connect(endpoint, 0);
    }

    @Override
    public void close() throws IOException {
        if (fd == -1)
            return;
        
        close(fd);
        fd = -1;
    }
    
    public void write(byte[] bytes) throws IOException {
        send(fd, bytes, 0);
    }
    
    /**
     * 
     * @param buffer
     * @param block
     * @return # of bytes received, -1 or error
     * @throws IOException 
     */
    public int read(byte[] buffer) throws IOException {
        return recv(fd, buffer, MSG_DONTWAIT);
    }
    
    public boolean waitReadable(int timeout) throws IOException {
        int r = poll(fd, POLLIN | POLLERR | POLLHUP, timeout);
        
        if ((r & POLLIN) != 0) {
            return true;
        }
        
        if ((r & POLLHUP) != 0) {
            throw new IOException("Connection closed");
        }
        
        if ((r & POLLERR) != 0) {
            throw new IOException("Socket error");
        }
        
        return false;
    }

    private static native int getIntConst(String name);
    
    public static final int AF_INET = getIntConst("AF_INET");
    public static final int AF_INET6 = getIntConst("AF_INET6");
    
    public static final int SOCK_STREAM = getIntConst("SOCK_STREAM");
    
    public static final int MSG_DONTWAIT = getIntConst("MSG_DONTWAIT");
    
    public static final int POLLIN = getIntConst("POLLIN");
    public static final int POLLPRI = getIntConst("POLLPRI");
    public static final int POLLOUT = getIntConst("POLLOUT");
    public static final int POLLRDHUP = getIntConst("POLLRDHUP");
    public static final int POLLERR = getIntConst("POLLERR");
    public static final int POLLHUP = getIntConst("POLLHUP");
    public static final int POLLNVAL = getIntConst("POLLNVAL");
    public static final int POLLRDNORM = getIntConst("POLLRDNORM");
    public static final int POLLRDBAND = getIntConst("POLLRDBAND");
    public static final int POLLWRNORM = getIntConst("POLLWRNORM");
    public static final int POLLWRBAND = getIntConst("POLLWRBAND");
    
    public static final int EAGAIN = getIntConst("EAGAIN");
    public static final int EINPROGRESS = getIntConst("EINPROGRESS");

    public static void main(String[] args) throws IOException {
        try (NativeSocket s = new NativeSocket()) {
            s.connect(new InetSocketAddress("localhost", 23), 30000);
            
            while (true) {
                if (s.waitReadable(1000)) {
                    byte[] b = new byte[16];
                    int nr = s.read(b);
                    if (nr > 0) {
                        b = Arrays.copyOf(b, nr);
                        s.write(b);
                    }
                    
                    if (nr == 0) {
                        break;
                    }

                    if (nr == -1) {
                        System.out.println("Error? nr=-1");
                    }
                } else {
                    s.write("no data\n".getBytes());
                }
            }
        }
        
    }
    
    private static native int socket(int domain, int type, int protocol) throws IOException;
    private static native void close(int fd) throws IOException;
    private static native void configureBlocking(int fd, boolean block);
    private static native void connect(int fd, int domain, byte[] address, int port) throws IOException;
    private static native int send(int fd, byte[] buf, int flags) throws IOException;
    private static native int recv(int fd, byte[] buf, int flags) throws IOException;
    
    private static native int poll(int fd, int events, int timeout) throws IOException;

}
