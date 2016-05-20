/*
 * Copyright (C) 2016 Nuke Sparrow <nukesparrow@bitmessage.ch>.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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
