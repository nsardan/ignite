// @java.file.header

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.design.plugin;

import org.gridgain.grid.*;
import org.gridgain.grid.design.*;
import org.gridgain.grid.kernal.managers.communication.*;
import org.gridgain.grid.logger.*;
import org.gridgain.grid.spi.*;
import org.gridgain.grid.spi.discovery.*;

import java.io.*;
import java.util.*;

/**
 * TODO: Add class description.
 */
public interface PluginContext {
    /**
     * @return Plugin configuration.
     */
    public PluginConfiguration configuration();

    /**
     * @return Grid.
     */
    public Grid grid();

    /**
     * Gets a collection of all grid nodes. Remote nodes are discovered via underlying
     * {@link GridDiscoverySpi} implementation used.
     *
     * @return Collection of grid nodes.
     * @see #localNode()
     * @see GridDiscoverySpi
     */
    public Collection<GridNode> nodes();

    /**
     * Gets local grid node. Instance of local node is provided by underlying {@link GridDiscoverySpi}
     * implementation used.
     *
     * @return Local grid node.
     * @see GridDiscoverySpi
     */
    public GridNode localNode();

    /**
     * Gets logger for given class.
     *
     * @param cls Class to get logger for.
     * @return Logger.
     */
    public GridLogger log(Class<?> cls);

    /**
     * Registers open port.
     *
     * @param port Port.
     * @param proto Protocol.
     * @param cls Class.
     */
    public void registerPort(int port, GridPortProtocol proto, Class<?> cls);

    /**
     * Deregisters closed port.
     *
     * @param port Port.
     * @param proto Protocol.
     * @param cls Class.
     */
    public void deregisterPort(int port, GridPortProtocol proto, Class<?> cls);

    /**
     * Deregisters all closed ports.
     *
     * @param cls Class.
     */
    public void deregisterPorts(Class<?> cls);
}
