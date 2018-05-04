package de.tischner.cobweb.routing.algorithms.metrics.landmark;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.tischner.cobweb.routing.algorithms.metrics.IMetric;
import de.tischner.cobweb.routing.algorithms.shortestpath.IShortestPathComputation;
import de.tischner.cobweb.routing.algorithms.shortestpath.dijkstra.Dijkstra;
import de.tischner.cobweb.routing.algorithms.shortestpath.dijkstra.IHasPathCost;
import de.tischner.cobweb.routing.model.graph.IEdge;
import de.tischner.cobweb.routing.model.graph.IGraph;
import de.tischner.cobweb.routing.model.graph.INode;
import de.tischner.cobweb.util.NestedMap;

public final class LandmarkMetric<N extends INode, E extends IEdge<N>, G extends IGraph<N, E>> implements IMetric<N> {

  private final static Logger LOGGER = LoggerFactory.getLogger(LandmarkMetric.class);
  private Collection<N> mLandmarks;
  private final NestedMap<N, N, Double> mLandmarkToNodeDistance;
  private final NestedMap<N, N, Double> mNodeToLandmarkDistance;

  public LandmarkMetric(final int amount, final G graph, final ILandmarkProvider<N> landmarkProvider) {
    mLandmarkToNodeDistance = new NestedMap<>();
    mNodeToLandmarkDistance = new NestedMap<>();

    initialize(amount, graph, landmarkProvider, new Dijkstra<>(graph));
  }

  @Override
  public double distance(final N first, final N second) {
    double greatestDistance = 0.0;
    for (final N landmark : mLandmarks) {
      final Double firstToLandmark = mNodeToLandmarkDistance.get(first, landmark);
      final Double secondToLandmark = mNodeToLandmarkDistance.get(second, landmark);
      final Double landmarkToSecond = mLandmarkToNodeDistance.get(landmark, second);
      final Double landmarkToFirst = mLandmarkToNodeDistance.get(landmark, first);

      // Ignore the landmark if anyone can not reach it
      if (firstToLandmark == null || secondToLandmark == null || landmarkToSecond == null || landmarkToFirst == null) {
        continue;
      }

      final double landmarkBehindDestination = firstToLandmark - secondToLandmark;
      final double landmarkBeforeSource = landmarkToSecond - landmarkToFirst;
      final double distance = Math.max(landmarkBehindDestination, landmarkBeforeSource);
      if (distance > greatestDistance) {
        greatestDistance = distance;
      }
    }

    return greatestDistance;
  }

  private void initialize(final int amount, final G graph, final ILandmarkProvider<N> landmarkProvider,
      final IShortestPathComputation<N, E> computation) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Computing landmarks");
    }
    mLandmarks = landmarkProvider.getLandmarks(amount);

    // Compute distances from landmarks to all other nodes
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Computing distances from {} landmarks to {} nodes", mLandmarks.size(), graph.size());
    }
    for (final N landmark : mLandmarks) {
      final Map<N, ? extends IHasPathCost> nodeToDistance = computation.computeShortestPathCostsReachable(landmark);
      for (final Entry<N, ? extends IHasPathCost> entry : nodeToDistance.entrySet()) {
        mLandmarkToNodeDistance.put(landmark, entry.getKey(), entry.getValue().getPathCost());
      }
    }

    // Compute distances from all nodes to landmarks
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Computing distances from {} nodes to {} landmarks", graph.size(), mLandmarks.size());
    }
    graph.reverse();
    for (final N landmark : mLandmarks) {
      final Map<N, ? extends IHasPathCost> nodeToDistance = computation.computeShortestPathCostsReachable(landmark);
      for (final Entry<N, ? extends IHasPathCost> entry : nodeToDistance.entrySet()) {
        mNodeToLandmarkDistance.put(entry.getKey(), landmark, entry.getValue().getPathCost());
      }
    }
    graph.reverse();
  }
}
