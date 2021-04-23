/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.server;

import com.facebook.airlift.log.Logger;
import com.facebook.presto.connector.ConnectorManager;
import com.facebook.presto.metadata.DynamicCatalogStore;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import static com.facebook.presto.server.PrestoServer.updateConnectorIds;
import static java.util.Objects.requireNonNull;

@Path("/v1/catalog")
public class CatalogResource
{
    private static final Logger log = Logger.get(CatalogResource.class);
    private final ConnectorManager connectorManager;
    private final DynamicCatalogStore dynamicCatalogStore;

    @Inject
    public CatalogResource(
            ConnectorManager connectorManager,
            DynamicCatalogStore dynamicCatalogStore)
    {
        this.connectorManager = requireNonNull(connectorManager, "connectorManager is null");
        this.dynamicCatalogStore = requireNonNull(dynamicCatalogStore, "dynamicCatalogStore is null");
    }

    @POST
    @Path("{catalogName}")
    public void addCatalog(@PathParam("catalogName") String catalogName)
    {
        log.info("-- add catalog {}", catalogName);
        requireNonNull(catalogName, "catalogName is null");
        dynamicCatalogStore.loadCatalog(catalogName);
        updateConnectorIds(catalogName, PrestoServer.ConnectorAction.ADD);
    }

    @DELETE
    @Path("{catalogName}")
    public void dropCatalog(@PathParam("catalogName") String catalogName)
    {
        log.info("-- drop catalog {}", catalogName);
        requireNonNull(catalogName, "catalogName is null");
        connectorManager.dropConnection(catalogName);
        updateConnectorIds(catalogName, PrestoServer.ConnectorAction.DELETE);
    }

    @PUT
    @Path("{catalogName}")
    public void updateCatalog(@PathParam("catalogName") String catalogName)
    {
        log.info("-- update catalog {}", catalogName);
        requireNonNull(catalogName, "catalogName is null");
        connectorManager.dropConnection(catalogName);
        dynamicCatalogStore.loadCatalog(catalogName);
    }
}
