package com.rapleaf.hank.coordinator.zk;

import com.rapleaf.hank.ZkTestCase;
import com.rapleaf.hank.coordinator.HostConfig;
import com.rapleaf.hank.coordinator.HostDomainConfig;
import com.rapleaf.hank.coordinator.HostState;
import com.rapleaf.hank.coordinator.PartDaemonAddress;
import com.rapleaf.hank.coordinator.PartDaemonState;
import com.rapleaf.hank.coordinator.UpdateDaemonState;
import com.rapleaf.hank.coordinator.HostConfig.HostStateChangeListener;

public class TestZkHostConfig extends ZkTestCase {
  private static final class MockHostStateChangeListener implements HostStateChangeListener {
    private HostConfig calledWith;

    @Override
    public void onHostStateChange(HostConfig hostConfig) {
      calledWith = hostConfig;
      synchronized (this) {
        notifyAll();
      }
    }
  }

  private final String HOST = getRoot() + "/my.super.host:32267";
  private static final PartDaemonAddress ADDRESS = new PartDaemonAddress("my.super.host", 32267);

  public void testLoadAndState() throws Exception {
    ZkHostConfig c = ZkHostConfig.create(getZk(), getRoot(), ADDRESS);
    assertEquals(ADDRESS, c.getAddress());
    assertEquals(PartDaemonState.IDLE, c.getPartDaemonState());
    assertEquals(UpdateDaemonState.IDLE, c.getUpdateDaemonState());
    assertEquals(HostState.IDLE, c.getHostState());

    c = new ZkHostConfig(getZk(), HOST);
    assertEquals(ADDRESS, c.getAddress());
    assertEquals(HostState.IDLE, c.getHostState());
    assertEquals(PartDaemonState.IDLE, c.getPartDaemonState());
    assertEquals(UpdateDaemonState.IDLE, c.getUpdateDaemonState());

    c.setPartDaemonState(PartDaemonState.STARTABLE);
    c.setUpdateDaemonState(UpdateDaemonState.UPDATING);
    c = new ZkHostConfig(getZk(), HOST);
    assertEquals(PartDaemonState.STARTABLE, c.getPartDaemonState());
    assertEquals(UpdateDaemonState.UPDATING, c.getUpdateDaemonState());
  }

  public void testStateChangeListener() throws Exception {
    ZkHostConfig c = ZkHostConfig.create(getZk(), getRoot(), ADDRESS);
    MockHostStateChangeListener mockListener = new MockHostStateChangeListener();
    c.setStateChangeListener(mockListener);

    synchronized (mockListener) {
      mockListener.wait(100);
    }

    assertNull("should not receive a callback until something is changed...", mockListener.calledWith);

    c.setPartDaemonState(PartDaemonState.STARTABLE);
    synchronized (mockListener) {
      mockListener.wait(1000);
    }
    assertNotNull("mock listener should have received a call!", mockListener.calledWith);
    assertEquals(ADDRESS, mockListener.calledWith.getAddress());
    assertEquals(PartDaemonState.STARTABLE, mockListener.calledWith.getPartDaemonState());

    mockListener.calledWith = null;
    c.setPartDaemonState(PartDaemonState.STARTING);
    synchronized (mockListener) {
      mockListener.wait(1000);
    }
    assertNotNull("mock listener should have received second call!", mockListener.calledWith);
    assertEquals(ADDRESS, mockListener.calledWith.getAddress());
    assertEquals(PartDaemonState.STARTING, mockListener.calledWith.getPartDaemonState());
  }

  public void testDomains() throws Exception {
    ZkHostConfig c = ZkHostConfig.create(getZk(), getRoot(), ADDRESS);
    assertEquals(0, c.getAssignedDomains().size());

    c.addDomain((byte) 0);
    HostDomainConfig hostDomainConf = (HostDomainConfig) c.getAssignedDomains().toArray()[0];
    assertEquals(0, hostDomainConf.getDomainId());

    assertEquals(0, c.getDomainById(0).getDomainId());
  }

  public void testOnline() throws Exception {
    ZkHostConfig host = ZkHostConfig.create(getZk(), getRoot(), ADDRESS);
    assertFalse(host.isUpdateDaemonOnline());
    host.updateDaemonOnline();
    assertTrue(host.isUpdateDaemonOnline());
    host.updateDaemonOffline();
    assertFalse(host.isUpdateDaemonOnline());

    assertFalse(host.isPartDaemonOnline());
    host.partDaemonOnline();
    assertTrue(host.isPartDaemonOnline());
    host.partDaemonOffline();
    assertFalse(host.isPartDaemonOnline());
  }
}
