/*
 * Frightening License 1.0
 * 
 * This software probably contains trojans and backdoors. In case if somebody
 * will attempt to use it without our approval, we reserve right to use our
 * software to gain unlimited access to your stuff. Feel the fear. Maybe even
 * viewing this code could put your system in danger. Who knowns...
 */
package ns.nativesockets;

import java.io.IOException;

/**
 *
 * @author Nuke Sparrow <nukesparrow@bitmessage.ch>
 */
public class NativeSocketIOException extends IOException {

    public NativeSocketIOException(String message) {
        super(message);
        errno = 0;
    }

    public NativeSocketIOException(int errno, String message) {
        super(message);
        this.errno = errno;
    }

    private int errno;

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

}
