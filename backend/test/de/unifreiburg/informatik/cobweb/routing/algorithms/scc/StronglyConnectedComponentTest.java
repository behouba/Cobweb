package de.unifreiburg.informatik.cobweb.routing.algorithms.scc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.unifreiburg.informatik.cobweb.routing.model.graph.BasicNode;

/**
 * Test for the class {@link StronglyConnectedComponent}.
 *
 * @author Daniel Tischner {@literal <zabuza.dev@gmail.com>}
 */
public final class StronglyConnectedComponentTest {

  /**
   * The SCC used for testing.
   */
  private StronglyConnectedComponent<BasicNode> mScc;

  /**
   * Setups a SCC instance for testing.
   */
  @Before
  public void setUp() {
    mScc = new StronglyConnectedComponent<>();
    final BasicNode root = new BasicNode(2);
    mScc.addNode(new BasicNode(1));
    mScc.addNode(root);
    mScc.addNode(new BasicNode(3));
    mScc.addNode(new BasicNode(4));
    mScc.setRootNode(root);
  }

  /**
   * Test method for
   * {@link de.unifreiburg.informatik.cobweb.routing.algorithms.scc.StronglyConnectedComponent#addNode(java.lang.Object)}.
   */
  @Test
  public void testAddNode() {
    final BasicNode node = new BasicNode(10);
    Assert.assertFalse(mScc.getNodes().contains(node));
    Assert.assertTrue(mScc.addNode(node));
    Assert.assertTrue(mScc.getNodes().contains(node));
    Assert.assertFalse(mScc.addNode(node));
    Assert.assertTrue(mScc.getNodes().contains(node));
  }

  /**
   * Test method for
   * {@link de.unifreiburg.informatik.cobweb.routing.algorithms.scc.StronglyConnectedComponent#getNodes()}.
   */
  @Test
  public void testGetNodes() {
    final BasicNode node = new BasicNode(10);
    Assert.assertFalse(mScc.getNodes().contains(node));
    Assert.assertEquals(4, mScc.getNodes().size());

    mScc.addNode(node);
    Assert.assertTrue(mScc.getNodes().contains(node));
    Assert.assertEquals(5, mScc.getNodes().size());

    final StronglyConnectedComponent<BasicNode> otherScc = new StronglyConnectedComponent<>();
    Assert.assertTrue(otherScc.getNodes().isEmpty());
  }

  /**
   * Test method for
   * {@link de.unifreiburg.informatik.cobweb.routing.algorithms.scc.StronglyConnectedComponent#getRootNode()}.
   */
  @Test
  public void testGetRootNode() {
    Assert.assertEquals(2L, mScc.getRootNode().getId());
  }

  /**
   * Test method for
   * {@link de.unifreiburg.informatik.cobweb.routing.algorithms.scc.StronglyConnectedComponent#setRootNode(java.lang.Object)}.
   */
  @Test
  public void testSetRootNode() {
    Assert.assertEquals(2L, mScc.getRootNode().getId());

    final BasicNode node = new BasicNode(10);
    mScc.addNode(node);
    mScc.setRootNode(node);
    Assert.assertEquals(10L, mScc.getRootNode().getId());

    final StronglyConnectedComponent<BasicNode> otherScc = new StronglyConnectedComponent<>();
    Assert.assertNull(otherScc.getRootNode());
  }

  /**
   * Test method for
   * {@link de.unifreiburg.informatik.cobweb.routing.algorithms.scc.StronglyConnectedComponent#size()}.
   */
  @Test
  public void testSize() {
    Assert.assertEquals(4, mScc.size());

    mScc.addNode(new BasicNode(10));
    Assert.assertEquals(5, mScc.size());

    final StronglyConnectedComponent<BasicNode> otherScc = new StronglyConnectedComponent<>();
    Assert.assertEquals(0, otherScc.size());
  }

  /**
   * Test method for
   * {@link de.unifreiburg.informatik.cobweb.routing.algorithms.scc.StronglyConnectedComponent#StronglyConnectedComponent()}.
   */
  @SuppressWarnings({ "unused", "static-method" })
  @Test
  public void testStronglyConnectedComponent() {
    try {
      new StronglyConnectedComponent<>();
    } catch (final Exception e) {
      Assert.fail();
    }
  }

}
