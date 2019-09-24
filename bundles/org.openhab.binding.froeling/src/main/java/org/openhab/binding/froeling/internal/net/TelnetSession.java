/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.froeling.internal.net;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.net.telnet.TelnetClient;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single telnet session.
 *
 * @author Allan Tong - Initial contribution
 */
@NonNullByDefault
public class TelnetSession implements Closeable {

    private Logger logger = LoggerFactory.getLogger(TelnetSession.class);
    private TelnetClient telnetClient = null;
    private NonblockingBufferedReader nbreader = null;

    public TelnetSession() {
        this.telnetClient = new TelnetClient();
    }

    public void open(String host, int port) throws IOException {
        if (this.telnetClient != null) {
            this.telnetClient.connect(host, port);
            this.telnetClient.setKeepAlive(true);

            this.nbreader = new NonblockingBufferedReader(new BufferedReader(
                    new InputStreamReader(this.telnetClient.getInputStream(), StandardCharsets.ISO_8859_1), 1024));
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.telnetClient != null) {
                if (isConnected()) {
                    this.telnetClient.setKeepAlive(false);
                }
                this.telnetClient.disconnect();
            }
        } catch (Exception e) {
            this.logger.error("Error closing telnetClient", e);
        }
    }

    public boolean isConnected() {
        if (this.telnetClient != null) {
            return this.telnetClient.isConnected();
        } else {
            return false;
        }
    }

    public String readline() throws IOException {
        String buffer = "";

        if (this.nbreader != null) {
            buffer = this.nbreader.readLine();
        }
        return buffer;
    }
}
