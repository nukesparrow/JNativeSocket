

ifeq ($(shell $(CC) -dumpmachine),i686-linux-gnu)
ARCH ?= i386-linux-gnu
else
endif

ifeq ($(ARCH),x86_64-linux-gnu)
JAVA_HOME ?= /usr/lib/jvm/java-1.9.0-openjdk-amd64
CFLAGS += -m64
LDFLAGS += -m64
else
ifeq ($(ARCH),i386-linux-gnu)
JAVA_HOME ?= /usr/lib/jvm/java-1.9.0-openjdk-i386
CFLAGS += -m32
LDFLAGS += -m32
else
ARCH ?= $(shell $(CC) -dumpmachine)
endif
endif

JAVAH ?= $(JAVA_HOME)/bin/javah

CSRC = .
JAVASRC = ../java

CFLAGS += -I$(JAVA_HOME)/include
CFLAGS += -I$(JAVA_HOME)/include/linux
CFLAGS += -I$(JAVASRC)
CFLAGS += -fPIC
CFLAGS += -Wall -Werror

NATIVECLASSES = ns.nativesockets.NativeSocket

HEADER = $(CSRC)/ns_nativesockets_NativeSocket.h
SOURCE = $(CSRC)/ns_nativesockets_NativeSocket.c
OBJECT = ns_nativesockets_NativeSocket-$(ARCH).o
LIBRARY = ../resources/ns/nativesockets/libNativeSocket-$(ARCH).so

.PHONY: all clean

all: $(LIBRARY)

headers:
	$(JAVAH) -classpath $(JAVASRC) -d $(CSRC) $(NATIVECLASSES)

clean:
	$(RM) $(OBJECT) $(LIBRARY) $(HEADER)

$(OBJECT): $(SOURCE)
	$(CC) $(CFLAGS) -c $< -o $@

$(LIBRARY): $(OBJECT)
	$(CC) -shared $(LDFLAGS) -o $@ $(OBJECT)
