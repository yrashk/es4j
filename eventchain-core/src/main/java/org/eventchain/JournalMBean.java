package org.eventchain;

import javax.management.openmbean.TabularData;

public interface JournalMBean {
    TabularData getEntities();
    String getName();
}
