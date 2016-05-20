
.PHONY: clean all

all:
	make ARCH=i386-linux-gnu all
	make ARCH=x86_64-linux-gnu all

clean:
	make ARCH=i386-linux-gnu clean
	make ARCH=x86_64-linux-gnu clean
