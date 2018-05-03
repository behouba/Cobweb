package de.tischner.cobweb.routing.model.graph.road;

import de.tischner.cobweb.parsing.osm.EHighwayType;
import de.tischner.cobweb.routing.model.graph.IEdge;
import de.tischner.cobweb.routing.model.graph.INode;
import de.tischner.cobweb.util.RoutingUtil;

public final class RoadEdge<N extends INode & IHasId & ISpatial> implements IEdge<N>, IHasId, IReversedConsumer {

  private double mCost;
  private final N mDestination;
  private final long mId;
  private final int mMaxSpeed;
  private IReversedProvider mReversedProvider;
  private final N mSource;
  private final EHighwayType mType;

  public RoadEdge(final long id, final N source, final N destination, final EHighwayType type, final int maxSpeed) {
    mId = id;
    mSource = source;
    mDestination = destination;
    mType = type;
    mMaxSpeed = maxSpeed;
    updateCost();
  }

  /*
   * (non-Javadoc)
   *
   * @see de.tischner.cobweb.model.graph.IEdge#getCost()
   */
  @Override
  public double getCost() {
    return mCost;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.tischner.cobweb.routing.model.graph.IEdge#getDestination()
   */
  @Override
  public N getDestination() {
    if (mReversedProvider != null && mReversedProvider.isReversed()) {
      return mSource;
    }
    return mDestination;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.tischner.cobweb.model.graph.road.IHasId#getId()
   */
  @Override
  public long getId() {
    return mId;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.tischner.cobweb.model.graph.IEdge#getSource()
   */
  @Override
  public N getSource() {
    if (mReversedProvider != null && mReversedProvider.isReversed()) {
      return mDestination;
    }
    return mSource;
  }

  @Override
  public void setReversedProvider(final IReversedProvider provider) {
    mReversedProvider = provider;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append("RoadEdge [id=");
    builder.append(mId);
    builder.append(", ");
    builder.append(getSource().getId());
    builder.append(" -(");
    builder.append(mCost);
    builder.append(")-> ");
    builder.append(getDestination().getId());
    builder.append("]");
    return builder.toString();
  }

  public void updateCost() {
    final double speed = RoutingUtil.getSpeedOfHighway(mType, mMaxSpeed);
    final double distance = RoutingUtil.distanceEquiRect(mSource, mDestination);
    mCost = RoutingUtil.travelTime(distance, speed);
  }

}
