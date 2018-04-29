package de.tischner.cobweb.routing.server;

import de.tischner.cobweb.config.IRoutingConfigProvider;
import de.tischner.cobweb.db.IRoutingDatabase;
import de.tischner.cobweb.routing.model.graph.IEdge;
import de.tischner.cobweb.routing.model.graph.IGraph;
import de.tischner.cobweb.routing.model.graph.INode;
import de.tischner.cobweb.routing.model.graph.road.ICanGetNodeById;
import de.tischner.cobweb.routing.model.graph.road.IHasId;
import de.tischner.cobweb.routing.model.graph.road.ISpatial;

public final class RoutingServer<N extends INode & IHasId & ISpatial, E extends IEdge<N> & IHasId, G extends IGraph<N, E> & ICanGetNodeById<N>> {

  private final IRoutingConfigProvider mConfig;
  private final IRoutingDatabase mDatabase;
  private final G mGraph;

  public RoutingServer(final IRoutingConfigProvider config, final G graph, final IRoutingDatabase database) {
    // TODO Should get some algorithm for the queries
    mConfig = config;
    mGraph = graph;
    mDatabase = database;
  }

  public void initialize() {
    // TODO Implement
  }

  public boolean isRunning() {
    // TODO Implement
    return false;
  }

  public void shutdown() {
    // TODO Implement
  }

  public void start() {
    // TODO Implement
  }

}
