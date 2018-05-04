package de.tischner.cobweb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.util.ContextInitializer;
import de.tischner.cobweb.config.ConfigLoader;
import de.tischner.cobweb.config.ConfigStore;
import de.tischner.cobweb.db.ExternalDatabase;
import de.tischner.cobweb.db.OsmDatabaseHandler;
import de.tischner.cobweb.parsing.DataParser;
import de.tischner.cobweb.parsing.ParseException;
import de.tischner.cobweb.parsing.osm.IOsmFileHandler;
import de.tischner.cobweb.parsing.osm.IOsmFilter;
import de.tischner.cobweb.routing.algorithms.metrics.IMetric;
import de.tischner.cobweb.routing.algorithms.metrics.landmark.ILandmarkProvider;
import de.tischner.cobweb.routing.algorithms.metrics.landmark.LandmarkMetric;
import de.tischner.cobweb.routing.algorithms.metrics.landmark.RandomLandmarks;
import de.tischner.cobweb.routing.algorithms.shortestpath.IShortestPathComputation;
import de.tischner.cobweb.routing.algorithms.shortestpath.dijkstra.AStar;
import de.tischner.cobweb.routing.model.graph.road.RoadEdge;
import de.tischner.cobweb.routing.model.graph.road.RoadGraph;
import de.tischner.cobweb.routing.model.graph.road.RoadNode;
import de.tischner.cobweb.routing.parsing.osm.IOsmRoadBuilder;
import de.tischner.cobweb.routing.parsing.osm.OsmRoadBuilder;
import de.tischner.cobweb.routing.parsing.osm.OsmRoadFilter;
import de.tischner.cobweb.routing.parsing.osm.OsmRoadHandler;
import de.tischner.cobweb.routing.server.RoutingServer;
import de.tischner.cobweb.util.SerializationUtil;

public final class Application {
  private static final Path LOGGER_CONFIG = Paths.get("backend", "res", "logging", "logConfig.xml");
  private final String[] mArgs;
  private IShortestPathComputation<RoadNode, RoadEdge<RoadNode>> mComputation;
  private ConfigStore mConfig;
  private ConfigLoader mConfigLoader;
  private ExternalDatabase mDatabase;
  private RoadGraph<RoadNode, RoadEdge<RoadNode>> mGraph;
  private Logger mLogger;

  private RoutingServer<RoadNode, RoadEdge<RoadNode>, RoadGraph<RoadNode, RoadEdge<RoadNode>>> mRoutingServer;

  public Application(final String[] args) {
    mArgs = args;
  }

  public void initialize() {
    initializeLogger();
    mLogger.info("Initializing application");
    try {
      final Instant initStartTime = Instant.now();
      mConfig = new ConfigStore();
      mConfigLoader = new ConfigLoader();
      mConfigLoader.loadConfig(mConfig);
      // Save to ensure old configuration files get new default parameter
      mConfigLoader.saveConfig(mConfig);

      // TODO Choose based on arguments what to do
      // TODO Provide clean caches command, provide reducer command
      initializeApi();
      final Instant initEndTime = Instant.now();
      mLogger.info("Initialization took: {}", Duration.between(initStartTime, initEndTime));
    } catch (final Throwable e) {
      mLogger.error("Error at initialization of application", e);
      throw e;
    }
  }

  public void shutdown() {
    // TODO Make sure this is always called
    mLogger.info("Shutting down application");
    try {
      mRoutingServer.shutdown();
      mDatabase.shutdown();
    } catch (final Throwable e) {
      mLogger.error("Error while shutting down database", e);
      throw e;
    }
  }

  public void start() {
    try {
      mLogger.info("Starting application");
      // TODO Do something, based on arguments
      mRoutingServer.start();
    } catch (final Throwable e) {
      mLogger.error("Error while starting application", e);
      throw e;
    }
  }

  private Iterable<IOsmFileHandler> createOsmDatabaseHandler() {
    final IOsmFileHandler databaseHandler = new OsmDatabaseHandler(mDatabase);
    return Collections.singletonList(databaseHandler);
  }

  private Iterable<IOsmFileHandler> createOsmRoutingHandler() throws ParseException {
    final IOsmRoadBuilder<RoadNode, RoadEdge<RoadNode>> roadBuilder = new OsmRoadBuilder<>(mGraph);
    final IOsmFilter roadFilter = new OsmRoadFilter(mConfig);
    final IOsmFileHandler roadHandler = new OsmRoadHandler<>(mGraph, roadFilter, roadBuilder, mDatabase);
    return Collections.singletonList(roadHandler);
  }

  private void initializeApi() throws ParseException {
    initializeDatabase();
    initializeGraph();

    // TODO Decide if parsing is needed by checking caches and setting up file
    // filter
    final DataParser dataParser = new DataParser(mConfig);
    // Add OSM handler
    createOsmDatabaseHandler().forEach(dataParser::addOsmHandler);
    createOsmRoutingHandler().forEach(dataParser::addOsmHandler);
    // Parse all data
    final Instant parseStartTime = Instant.now();
    dataParser.parseData();
    final Instant parseEndTime = Instant.now();
    mLogger.info("Parsing took: {}", Duration.between(parseStartTime, parseEndTime));

    // TODO Only do that if the graph had handler at all, i.e. did change
    serializeGraphIfDesired();

    mLogger.info("Graph size: {}", mGraph.getSizeInformation());

    initializeRouting();
  }

  private void initializeDatabase() throws ParseException {
    mLogger.info("Initializing database");
    mDatabase = new ExternalDatabase(mConfig);
    try {
      mDatabase.initialize();
    } catch (SQLException | IOException e) {
      throw new ParseException(e);
    }
  }

  private void initializeGraph() throws ParseException {
    mLogger.info("Initializing graph");

    final Path graphCache = mConfig.getGraphCache();
    if (!mConfig.useGraphCache() || !Files.isRegularFile(graphCache)) {
      mGraph = new RoadGraph<>();
      return;
    }

    // Deserialize graph
    mLogger.info("Deserializing graph from: {}", graphCache);
    final SerializationUtil<RoadGraph<RoadNode, RoadEdge<RoadNode>>> serializationUtil = new SerializationUtil<>();
    try {
      mGraph = serializationUtil.deserialize(graphCache);
    } catch (ClassNotFoundException | ClassCastException | IOException e) {
      throw new ParseException(e);
    }
  }

  private void initializeLogger() {
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, LOGGER_CONFIG.toString());
    mLogger = LoggerFactory.getLogger(Application.class);
  }

  private void initializeRouting() {
    mLogger.info("Initializing routing");

    // Create the shortest path algorithm
    final ILandmarkProvider<RoadNode> landmarkProvider = new RandomLandmarks<>(mGraph);
    // TODO Adjust the amount of landmarks, use some constant
    final IMetric<RoadNode> metric = new LandmarkMetric<>(100, mGraph, landmarkProvider);
    mComputation = new AStar<>(mGraph, metric);

    mRoutingServer = new RoutingServer<>(mConfig, mGraph, mComputation, mDatabase);
    mRoutingServer.initialize();
  }

  private void serializeGraphIfDesired() throws ParseException {
    if (!mConfig.useGraphCache()) {
      return;
    }

    final Path graphCache = mConfig.getGraphCache();
    mLogger.info("Serializing graph to: {}", graphCache);
    final SerializationUtil<RoadGraph<RoadNode, RoadEdge<RoadNode>>> serializationUtil = new SerializationUtil<>();
    try {
      serializationUtil.serialize(mGraph, graphCache);
    } catch (final IOException e) {
      throw new ParseException(e);
    }
  }

}
