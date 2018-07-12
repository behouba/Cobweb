package de.tischner.cobweb.routing.model.timetable;

import java.io.Serializable;
import java.util.List;

import org.eclipse.collections.impl.list.mutable.FastList;

import de.tischner.cobweb.routing.model.graph.IHasId;

public final class Trip implements IHasId, Serializable {
  /**
   * The serial version UID.
   */
  private static final long serialVersionUID = 1L;

  private final int mId;
  private final List<Connection> mSequence;

  public Trip(final int id) {
    mId = id;
    mSequence = FastList.newList();
  }

  public void addConnectionToSequence(final Connection connection) {
    mSequence.add(connection);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Trip)) {
      return false;
    }
    final Trip other = (Trip) obj;
    if (this.mId != other.mId) {
      return false;
    }
    return true;
  }

  @Override
  public int getId() {
    return mId;
  }

  public List<Connection> getSequence() {
    return mSequence;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.mId;
    return result;
  }
}
