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
package org.openhab.binding.resol.handler;

import java.net.InetAddress;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.resol.internal.config.IPBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.resol.vbus.Connection;
import de.resol.vbus.TcpDataSource;
import de.resol.vbus.TcpDataSourceProvider;

/**
 * @author Daniel Hillenbrand - Initial contribution
 */
public class IPBridgeHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(IPBridgeHandler.class);
    IPBridgeConfiguration config = getThing().getConfiguration().as(IPBridgeConfiguration.class);

    private Connection connection;

    public IPBridgeHandler(Bridge bridge) {
        super(bridge);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge configuration missing");
            return;
        }

        if (StringUtils.isEmpty(config.getIpAddress())) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "bridge address not specified");
            return;
        }
    }

    public Connection connect() {
        connection = null;
        try {
            // Create a connection to a LAN-enabled VBus device
            TcpDataSource dataSource = TcpDataSourceProvider
                    .fetchInformation(InetAddress.getByName(config.getIpAddress()), 500);
            dataSource.setLivePassword("vbus");
            connection = dataSource.connectLive(0, 0x0020);

            // Establish the connection
            this.logger.debug("Connecting to bridge");
            connection.connect();
            updateStatus(ThingStatus.ONLINE);
        } catch (Throwable ex) {
            this.logger.debug("{}", ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error while connecting to bridge");
        }
        return connection;
    }

    public void disconnect() {
        try {
            if (connection != null) {
                this.logger.debug("Disconnecting from bridge");
                connection.disconnect();
                connection = null;
            }
        } catch (Exception e) {
            this.logger.debug("{}", e);
        }
    }
}
