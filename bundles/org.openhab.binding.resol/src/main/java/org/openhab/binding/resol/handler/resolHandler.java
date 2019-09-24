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

import static org.openhab.binding.resol.internal.resolBindingConstants.*;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.resol.internal.config.ResolConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.resol.vbus.Connection;
import de.resol.vbus.ConnectionAdapter;
import de.resol.vbus.HeaderSet;
import de.resol.vbus.HeaderSetConsolidator;
import de.resol.vbus.HeaderSetConsolidatorListener;
import de.resol.vbus.Packet;
import de.resol.vbus.Specification;
import de.resol.vbus.Specification.PacketFieldValue;

/**
 * The {@link resolHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Hillenbrand - Initial contribution
 */
public class resolHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(resolHandler.class);
    private @Nullable ResolConfiguration config = null;
    private @Nullable IPBridgeHandler bridgeHandler = null;

    public @Nullable Connection connection = null;
    public @Nullable ConnectionAdapter conAdapter = null;
    public @Nullable HeaderSetConsolidator<Packet> hsc = null;
    public @Nullable HeaderSetConsolidatorListener<Packet> hscListener = null;

    public resolHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO: handle command
    }

    @Override
    public void initialize() {
        config = getConfigAs(ResolConfiguration.class);
        getIPBridgeHandler();

        updateStatus(ThingStatus.ONLINE);

        PollingSchedularService pSS = new PollingSchedularService();
        try {
            this.logger.debug("Entering pollingLoop");
            pSS.pollingLoop();
        } catch (Exception e) {
            this.logger.error("Error while polling: {}", e);
        }
    }

    @NonNullByDefault
    public class PollingSchedularService {
        ResolConfiguration config = getThing().getConfiguration().as(ResolConfiguration.class);
        private Logger logger = LoggerFactory.getLogger(PollingSchedularService.class);

        public void pollingLoop() throws Exception {
            long initialDelay = 10;
            long pollingInterval = config.getPollingInterval().longValue();

            if (pollingInterval < 5 || pollingInterval > 3600) {
                this.logger.warn("Invalid polling rate: {}. Using 5 seconds.", pollingInterval);
                pollingInterval = 5;
            }

            this.logger.info("Starting PollingService with interval: {} seconds", pollingInterval);
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(new PollingService(), initialDelay, pollingInterval, TimeUnit.SECONDS);
        }
    }

    @NonNullByDefault
    class PollingService implements Runnable {
        @Override
        public void run() {
            // Get and parse data from resol controller
            getData();
        }
    }

    public void getData() {

        try {
            if (bridgeHandler == null) {
                this.logger.error("BridgeHandler not yet available");
                getIPBridgeHandler();
                return;
            }

            if (connection == null
                    || !connection.getConnectionState().equals(de.resol.vbus.Connection.ConnectionState.CONNECTED)) {
                connection = bridgeHandler.connect();
            }

            if (connection != null) {
                // Create a HeaderSetConsolidator that will hold all recently received packets
                hsc = new HeaderSetConsolidator<Packet>(0, Long.MIN_VALUE, Long.MAX_VALUE, 0, 0);

                // Add a listener to the Connection to monitor state changes and
                // add incoming packets to the HeaderSetConsolidator
                conAdapter = new ConnectionAdapter() {
                    private Logger logger = LoggerFactory.getLogger(resolHandler.class);

                    @Override
                    public void connectionStateChanged(de.resol.vbus.Connection connection) {
                        // this.logger.debug("connectionStateChanged: " + connection.getConnectionState());
                    }

                    @Override
                    public void packetReceived(de.resol.vbus.Connection connection, Packet packet) {
                        this.logger.debug("packetReceived: {}", packet.getId());
                        hsc.addHeader(packet);
                    }
                };

                connection.addListener(conAdapter);

                hscListener = new HeaderSetConsolidatorListener<Packet>() {
                    @Override
                    public void headerAdded(HeaderSet<Packet> headerSet, Packet header) {
                    }

                    @Override
                    public void headerSetProcessed(HeaderSetConsolidator<Packet> hsc) {
                        processHeaderSet(hsc);
                    }
                };

                // Add a listener to the HeaderSetConsolidator to get a
                // notification when the timer interval elapsed
                hsc.addListener(hscListener);

                // Start the timer, causing the "headerSetProcessed" listener
                // callback to be called at the given interval
                hsc.startTimer(10000);
            } else {
                this.logger.debug("Failed connecting to bridge");
            }
        } catch (Exception e) {
            this.logger.debug("{}", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "failed getting data from controller");
        }
    }

    public void processHeaderSet(HeaderSet<Packet> hs) {
        Logger logger = LoggerFactory.getLogger(resolHandler.class);
        logger.debug("------ {} ------", new Date(hs.getTimestamp()));
        // logger.debug("headerSet.getId() => " + hs.getId());
        // logger.debug("headerSet.getIdHash() => " + hs.getIdHash());

        Packet[] packets = null;
        packets = hs.getSortedHeaders(new Packet[hs.getHeaderCount()]);

        Specification spec = null;
        spec = Specification.getDefaultSpecification();

        PacketFieldValue[] pfvs = null;
        pfvs = spec.getPacketFieldValuesForHeaders(packets);
        // logger.debug("packetFieldValues.length => " + pfvs.length);

        try {
            for (PacketFieldValue pfv : pfvs) {
                String id = pfv.getPacketFieldId();
                double rawValue = pfv.getRawValueDouble();
                String name = pfv.getName();
                String text = pfv.formatTextValue(null, null);
                double temp = 0;

                logger.info("Id: {}, Name: {}, Raw: {}, Text: {}", id, name, rawValue, text);

                switch (pfv.getPacketFieldId()) {
                    /*
                     * Sonnenkraft SKSC2
                     */

                    // Sensor 1
                    case "00_0010_4214_10_0100_000_2_0":
                    case "00_0010_4278_10_0100_000_2_0":
                    case "00_0010_427B_10_0100_000_2_0":
                        temp = Math.round(rawValue * 100) / 100.0;
                        updateState(thing.getChannel(TEMPERATURE_S1).getUID(), new DecimalType(Double.toString(temp)));
                        break;

                    // Sensor 2
                    case "00_0010_4214_10_0100_002_2_0":
                    case "00_0010_4278_10_0100_002_2_0":
                    case "00_0010_427B_10_0100_002_2_0":
                        temp = Math.round(rawValue * 100) / 100.0;
                        updateState(thing.getChannel(TEMPERATURE_S2).getUID(), new DecimalType(Double.toString(temp)));
                        break;

                    // Sensor 3
                    case "00_0010_4214_10_0100_004_2_0":
                    case "00_0010_427B_10_0100_004_2_0":
                        temp = Math.round(rawValue * 100) / 100.0;
                        updateState(thing.getChannel(TEMPERATURE_S3).getUID(), new DecimalType(Double.toString(temp)));
                        break;

                    // Sensor 4
                    case "00_0010_4214_10_0100_006_2_0":
                    case "00_0010_427B_10_0100_006_2_0":
                        temp = Math.round(rawValue * 100) / 100.0;
                        updateState(thing.getChannel(TEMPERATURE_S4).getUID(), new DecimalType(Double.toString(temp)));
                        break;

                    // Temperature VFD 1
                    case "00_0010_4214_10_0100_024_2_0":
                        temp = Math.round(rawValue * 100) / 100.0;
                        updateState(thing.getChannel(TEMPERATURE_VFD1).getUID(),
                                new DecimalType(Double.toString(temp)));
                        break;

                    // Flow rate VFD 1
                    case "00_0010_4214_10_0100_026_2_0":
                        updateState(thing.getChannel(VOLUMETRICFLOWRATE_VFD1).getUID(),
                                new DecimalType(Double.toString(rawValue)));
                        break;

                    // Speed relais 1
                    case "00_0010_4214_10_0100_008_1_0":
                    case "00_0010_427B_10_0100_008_1_0":
                        updateState(thing.getChannel(SPEED_RELAIS1).getUID(),
                                new DecimalType(Double.toString(rawValue)));
                        break;

                    // Speed relais 2
                    case "00_0010_4214_10_0100_009_1_0":
                    case "00_0010_427B_10_0100_012_1_0":
                        updateState(thing.getChannel(SPEED_RELAIS2).getUID(),
                                new DecimalType(Double.toString(rawValue)));
                        break;

                    // Voltage 10V
                    case "00_0010_4214_10_0100_032_1_0":
                        updateState(thing.getChannel(VOLTAGE_10V).getUID(), new DecimalType(Double.toString(rawValue)));
                        break;

                    // Error mask
                    case "00_0010_4214_10_0100_010_1_0":
                    case "00_0010_427B_10_0100_020_2_0":
                        updateState(thing.getChannel(ERRORMASK).getUID(), new StringType(Double.toString(rawValue)));
                        break;

                    // Operating hours relais 1
                    case "00_0010_4214_10_0100_012_2_0":
                    case "00_0010_4278_10_0100_012_2_0":
                    case "00_0010_427B_10_0100_010_2_0":
                        updateState(thing.getChannel(OPERATINGHOURS_RELAIS1).getUID(),
                                new DecimalType(Double.toString(rawValue)));
                        break;

                    // Operating hours relais 2
                    case "00_0010_4214_10_0100_014_2_0":
                    case "00_0010_427B_10_0100_014_2_0":
                        updateState(thing.getChannel(OPERATINGHOURS_RELAIS2).getUID(),
                                new DecimalType(Double.toString(rawValue)));
                        break;

                    // Heat supplied
                    case "00_0010_4214_10_0100_016_2_0":
                    case "00_0010_427B_10_0100_028_4_0":
                        updateState(thing.getChannel(HEATSUPPLIED).getUID(),
                                new DecimalType(Double.toString(rawValue)));
                        break;

                    // SW-Version
                    case "00_0010_427B_10_0100_032_2_0":
                        updateState(thing.getChannel(SW_VERSION).getUID(), new StringType(Double.toString(rawValue)));
                        break;

                    // Variant
                    case "00_0010_427B_10_0100_034_2_0":
                        updateState(thing.getChannel(VARIANT).getUID(), new StringType(Double.toString(rawValue)));
                        break;

                    // Unit Type
                    case "00_0010_427B_10_0100_016_1_0":
                        updateState(thing.getChannel(UNIT_TYPE).getUID(), new StringType(Double.toString(rawValue)));
                        break;

                    // System
                    case "00_0010_427B_10_0100_017_1_0":
                        updateState(thing.getChannel(SYSTEM).getUID(), new StringType(Double.toString(rawValue)));
                        break;

                    // System time
                    case "00_0010_427B_10_0100_022_2_0":
                        updateState(thing.getChannel(SYSTEM_TIME).getUID(), new StringType(Double.toString(rawValue)));
                        break;

                    // Sensor 1 broken
                    case "00_0010_427B_10_0100_020_1_1":
                        updateState(thing.getChannel(S1_BROKEN).getUID(), new DecimalType(Double.toString(rawValue)));
                        break;

                    // Sensor 2 broken
                    case "00_0010_427B_10_0100_020_1_2":
                        updateState(thing.getChannel(S2_BROKEN).getUID(), new DecimalType(Double.toString(rawValue)));
                        break;

                    // Sensor 3 broken
                    case "00_0010_427B_10_0100_020_1_4":
                        updateState(thing.getChannel(S3_BROKEN).getUID(), new DecimalType(Double.toString(rawValue)));
                        break;

                    // Sensor 4 broken
                    case "00_0010_427B_10_0100_020_1_8":
                        updateState(thing.getChannel(S4_BROKEN).getUID(), new DecimalType(Double.toString(rawValue)));
                        break;

                    // Status mask
                    case "00_0010_4278_10_0100_022_1_0":
                    case "00_0010_427B_10_0100_024_4_0":
                        updateState(thing.getChannel(STATUSMASK).getUID(), new DecimalType(Double.toString(temp)));
                        break;

                    default:
                        this.logger.warn("No channel available for ID {} with name {} and value {}", id, name, text);
                }

            }
        } catch (Exception e) {
            this.logger.debug("{}", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error while parsing controller data");
            hsc.removeListener(hscListener);
            connection.removeListener(conAdapter);
            bridgeHandler.disconnect();
            return;
        }

        if (pfvs.length > 0) {
            updateState(thing.getChannel(CHANNEL_LASTUPDATE).getUID(), new DateTimeType());
            hsc.removeListener(hscListener);
            connection.removeListener(conAdapter);
            bridgeHandler.disconnect();
        }
    }

    private synchronized IPBridgeHandler getIPBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                logger.error("Required bridge not defined");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Required bridge not defined");
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof IPBridgeHandler) {
                this.bridgeHandler = (IPBridgeHandler) handler;
            } else {
                logger.error("BridgeHandler for bridge {} not available", bridge.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "BridgeHandler not available");
                return null;
            }
        }
        return this.bridgeHandler;
    }
}
