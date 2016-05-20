#define _GNU_SOURCE

#include <sys/socket.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <poll.h>
#include <stdint.h>

#include <jni.h>
//#include "ns_nativesockets_NativeSocket.h"

/*
 * Class:     ns_nativesockets_NativeSocket
 * Method:    getIntConst
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL
Java_ns_nativesockets_NativeSocket_getIntConst(JNIEnv *env, jclass class, jstring jname) {
    jint retVal = -1;

    const char *name = (*env)->GetStringUTFChars(env, jname, 0);

    if (!strcmp(name, "AF_INET")) {
        retVal = AF_INET;
    } else if (!strcmp(name, "AF_INET6")) {
        retVal = AF_INET6;
    } else if (!strcmp(name, "SOCK_STREAM")) {
        retVal = SOCK_STREAM;
    } else if (!strcmp(name, "SOCK_DGRAM")) {
        retVal = SOCK_DGRAM;
    } else if (!strcmp(name, "MSG_DONTWAIT")) {
        retVal = MSG_DONTWAIT;
    } else if (!strcmp(name, "POLLIN")) {
        retVal = POLLIN;
    } else if (!strcmp(name, "POLLPRI")) {
        retVal = POLLPRI;
    } else if (!strcmp(name, "POLLOUT")) {
        retVal = POLLOUT;
#ifdef _GNU_SOURCE
    } else if (!strcmp(name, "POLLRDHUP")) {
        retVal = POLLRDHUP;
#endif
    } else if (!strcmp(name, "POLLERR")) {
        retVal = POLLERR;
    } else if (!strcmp(name, "POLLHUP")) {
        retVal = POLLHUP;
    } else if (!strcmp(name, "POLLNVAL")) {
        retVal = POLLNVAL;
#ifdef _XOPEN_SOURCE
    } else if (!strcmp(name, "POLLRDNORM")) {
        retVal = POLLRDNORM;
    } else if (!strcmp(name, "POLLRDBAND")) {
        retVal = POLLRDBAND;
    } else if (!strcmp(name, "POLLWRNORM")) {
        retVal = POLLWRNORM;
    } else if (!strcmp(name, "POLLWRBAND")) {
        retVal = POLLWRBAND;
#endif // _XOPEN_SOURCE
    } else if (!strcmp(name, "EAGAIN")) {
        retVal = EAGAIN;
    } else if (!strcmp(name, "EINPROGRESS")) {
        retVal = EINPROGRESS;
    }

    (*env)->ReleaseStringUTFChars(env, jname, name);

    return retVal;
}

jint throwIOException(JNIEnv *env, char *message) {
    jclass exClass;
    char *className = "java/io/IOException";

    exClass = (*env)->FindClass(env, className);

    return (*env)->ThrowNew(env, exClass, message);
}

jint throwIOExceptionWithErrno(JNIEnv *env, char *message) {
    jclass exClass;
    char *className = "ns/nativesockets/NativeSocketIOException";

    exClass = (*env)->FindClass(env, className);

    jmethodID exConstr = (*env)->GetMethodID(env, exClass, "<init>", "(ILjava/lang/String;)V");

    jobject ex = (*env)->NewObject(env, exClass, exConstr, errno, (*env)->NewStringUTF(env, message));

    return (*env)->Throw(env, ex);
}

/*
 * Class:     ns_nativesockets_NativeSocket
 * Method:    socket
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_ns_nativesockets_NativeSocket_socket
(JNIEnv *env, jclass class, jint domain, jint type, jint protocol) {
    int r = socket(domain, type, protocol);

    if (r == -1) {
        throwIOExceptionWithErrno(env, strerror(errno));
        return -1;
    }

    return r;
}

/*
 * Class:     ns_nativesockets_NativeSocket
 * Method:    close
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_ns_nativesockets_NativeSocket_close
(JNIEnv *env, jclass class, jint fd) {
    if (close(fd) == -1) {
        throwIOExceptionWithErrno(env, strerror(errno));
    }
}

/*
 * Class:     ns_nativesockets_NativeSocket
 * Method:    configureBlocking
 * Signature: (IZ)V
 */
JNIEXPORT void JNICALL Java_ns_nativesockets_NativeSocket_configureBlocking
(JNIEnv *env, jclass class, jint fd, jboolean block) {
    int flags = fcntl(fd, F_GETFL, 0);
    if (flags < 0) {
        throwIOExceptionWithErrno(env, strerror(errno));
        return;
    };
    flags = block ? (flags&~O_NONBLOCK) : (flags | O_NONBLOCK);
    if (fcntl(fd, F_SETFL, flags) == -1) {
        throwIOExceptionWithErrno(env, strerror(errno));
        return;
    }
}

/*
 * Class:     ns_nativesockets_NativeSocket
 * Method:    connect
 * Signature: (II[BI)V
 */
JNIEXPORT void JNICALL Java_ns_nativesockets_NativeSocket_connect
(JNIEnv *env, jclass class, jint fd, jint domain, jbyteArray address, jint port) {
    struct sockaddr_storage sa;
    struct sockaddr_in *sa_in = (struct sockaddr_in *)&sa;
    struct sockaddr_in6 *sa_in6 = (struct sockaddr_in6 *)&sa;
    socklen_t addrlen = sizeof(struct sockaddr_storage);
    
    jbyte buf[16];

    if (domain == AF_INET) {
        if ((*env)->GetArrayLength(env, address) != 4) {
            throwIOException(env, "Invalid IPv4 address length");
            return;
        }
        addrlen = sizeof(struct sockaddr_in);
        sa_in->sin_family = AF_INET;
        sa_in->sin_port = htons(port);
        (*env)->GetByteArrayRegion(env, address, 0, 4, buf);
        memcpy(&sa_in->sin_addr, buf, 4);
    }
    else if (domain == AF_INET6) {
        addrlen = sizeof(struct sockaddr_in6);
        if ((*env)->GetArrayLength(env, address) != 16) {
            throwIOException(env, "Invalid IPv6 address length");
            return;
        }
        sa_in6->sin6_family = AF_INET;
        sa_in6->sin6_port = htons(port);
        (*env)->GetByteArrayRegion(env, address, 0, 16, buf);
        memcpy(&sa_in6->sin6_addr, buf, 16);
    }
    else
    {
        throwIOException(env, "Unsupported domain");
        return;
    }
    
    if (connect(fd, (struct sockaddr*)&sa, addrlen) == -1) {
        throwIOExceptionWithErrno(env, strerror(errno));
        return;
    }
}

/*
 * Class:     ns_nativesockets_NativeSocket
 * Method:    send
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_ns_nativesockets_NativeSocket_send
  (JNIEnv *env, jclass class, jint fd, jbyteArray buf, jint flags)
{
    jint l = ((*env)->GetArrayLength(env, buf));
    jbyte buffer[l];

    (*env)->GetByteArrayRegion(env, buf, 0, l, buffer);

    ssize_t ns = send(fd, (void*)buffer, l, flags);
    
    if (ns == -1) {
        throwIOExceptionWithErrno(env, strerror(errno));
        return -1;
    }
    
    return ns;
}

/*
 * Class:     ns_nativesockets_NativeSocket
 * Method:    recv
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_ns_nativesockets_NativeSocket_recv
  (JNIEnv *env, jclass class, jint fd, jbyteArray buf, jint flags)
{
    jint l = ((*env)->GetArrayLength(env, buf));
    jbyte jbuf[l];

    ssize_t ns = recv(fd, (void*)jbuf, l, flags);
    
    if (ns == -1) {
        throwIOExceptionWithErrno(env, strerror(errno));
        return -1;
    }
    
    if (ns > 0) {
        (*env)->SetByteArrayRegion(env, buf, 0, ns, jbuf);
    }
    
    return ns;
}

/*
 * Class:     ns_nativesockets_NativeSocket
 * Method:    poll
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_ns_nativesockets_NativeSocket_poll
  (JNIEnv *env, jclass class, jint fd, jint events, jint timeout)
{
    struct pollfd pollfd;
    
    pollfd.fd = fd;
    pollfd.events = events;
    pollfd.revents = 0;
    
    if (poll(&pollfd, 1, timeout) == -1) {
        throwIOExceptionWithErrno(env, strerror(errno));
        return -1;
    }
    
    return pollfd.revents;
}
