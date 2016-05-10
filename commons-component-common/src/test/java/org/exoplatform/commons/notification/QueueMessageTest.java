package org.exoplatform.commons.notification;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.model.UserSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.QueueMessage;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.impl.service.QueueMessageImpl;
import org.exoplatform.commons.notification.job.NotificationJob;
import org.exoplatform.commons.notification.plugin.PluginTest;

public class QueueMessageTest extends BaseNotificationTestCase {
  
  private QueueMessage queueMessage;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    queueMessage = getService(QueueMessage.class);
  }
  
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }
  

  public void testCompressDecompress() throws Exception {

    String initialString = "abcdefghijklmnopqrstuvwxyzabcdeéabcd";

    Method compress = QueueMessageImpl.class.getDeclaredMethod("compress", String.class);
    compress.setAccessible(true);
    InputStream is = (InputStream)compress.invoke(null, initialString);

    Method decompress = QueueMessageImpl.class.getDeclaredMethod("decompress", InputStream.class);
    decompress.setAccessible(true);
    String result = (String)decompress.invoke(null,is);
    assertTrue(result.equals(initialString));
    
  }
}
