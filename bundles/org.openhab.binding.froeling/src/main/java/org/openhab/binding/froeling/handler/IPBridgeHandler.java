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
package org.openhab.binding.froeling.handler;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.froeling.internal.config.IPBridgeConfiguration;
import org.openhab.binding.froeling.internal.net.TelnetSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IPBridgeHandler} is responsible for communicating with
 * a Serial-LAN converter.
 *
 * @author Daniel Hillenbrand - Initial contribution
 */
@NonNullByDefault
public class IPBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(IPBridgeHandler.class);
    private TelnetSession session = null;
    IPBridgeConfiguration config = getThing().getConfiguration().as(IPBridgeConfiguration.class);

    public IPBridgeHandler(Bridge bridge) {
        super(bridge);
        this.session = new TelnetSession();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize() {
        this.scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                updateStatus(ThingStatus.ONLINE);
                if (config == null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "bridge configuration missing");
                    return;
                }

                if (StringUtils.isEmpty(config.getIpAddress())) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "bridge address not specified");
                    return;
                }

                if (config.getPort() <= 0) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "bridge port not specified");
                    return;
                }
            }
        }, 0, TimeUnit.SECONDS);
    }

    public synchronized void connect() {
        if (this.session != null) {
            if (this.session.isConnected()) {
                this.logger.info("Already connected to the bridge");
                return;
            }
        } else {
            this.logger.info("TelnetSession is null, creating new session");
            this.session = new TelnetSession();
        }
        this.logger.info("Connecting to bridge at {}:{}", config.getIpAddress(), config.getPort());
        try {
            this.session.open(config.getIpAddress(), config.getPort());
        } catch (IOException e) {
            this.logger.error("Failed to connect to bridge at {}:{}", config.getIpAddress(), config.getPort());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "failed to connect");
        }
        this.logger.debug("Connected to bridge at {}:{}", config.getIpAddress(), config.getPort());
        updateStatus(ThingStatus.ONLINE);
    }

    public boolean isConnected() {
        if (this.session != null) {
            this.logger.info("Bridge state: {}", this.session.isConnected());
            return this.session.isConnected();
        } else {
            return false;
        }
    }

    public synchronized void disconnect() {
        this.logger.info("Disconnecting from bridge");
        try {
            if (this.session != null) {
                this.session.close();
            }
        } catch (Exception e) {
            this.logger.error("Error disconnecting from bridge", e);
        }
    }

    private synchronized void reconnect() {
        this.logger.info("Attempting to reconnect to the bridge");
        disconnect();
        connect();
    }

    @Override
    public void dispose() {
        disconnect();
    }

    public String readInput() throws IOException {
        String buffer = "";
        if (this.session != null) {
            if (!this.session.isConnected()) {
                reconnect();
            } else {
                buffer = this.session.readline();
            }
        }
        return buffer;
    }
}
