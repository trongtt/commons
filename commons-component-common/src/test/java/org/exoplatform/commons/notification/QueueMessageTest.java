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

    String initialString = "{\"to\":\"John Smith<john.smith@acme.com>\",\"pluginId\":\"digest\",\"body\":\"<table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" width=\\\"600\\\" bgcolor=\\\"#ffffff\\\" align=\\\"center\\\" style=\\\"background-color: #ffffff; font-size: 13px;color:#333333;line-height: 18px;font-family: HelveticaNeue, Helvetica, Arial, sans-serif;\\\">\\n    <tr>\\n        <td align=\\\"center\\\"  valign=\\\"middle\\\" >\\n            <table  cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" width=\\\"100%\\\" bgcolor=\\\"#ffffff\\\" align=\\\"center\\\" style=\\\"border:1px solid #d8d8d8;\\\">\\n                <tr>\\n                    <td  height=\\\"45\\\" valign=\\\"middle\\\" style=\\\"margin:0;height:45px;font-weight:bold;vertical-align:middle; background-color: #efefef; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif;color:#2f5e92;font-size:18px;text-align:center\\\">\\n                        Vos <a target=\\\"_blank\\\" style=\\\"text-decoration: none; font-weight: bold; color: #2F5E92; \\\" href=\\\"http://localhost:8080/rest/social/notifications/redirectUrl/portal_home/eXo\\\">eXo<\\/a> notifications pour aujourd'hui\\n                    <\\/td>\\n                <\\/tr>\\n            <\\/table>\\n        <\\/td>\\n    <\\/tr><!--end header area-->\\n    <tr>\\n        <td bgcolor=\\\"#ffffff\\\" style=\\\"background-color: #ffffff;\\\">\\n            <table cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" width=\\\"100%\\\"  bgcolor=\\\"#ffffff\\\" style=\\\"background-color: #ffffff; border-left:1px solid #d8d8d8;border-right:1px solid #d8d8d8;\\\">\\n                <tr>\\n                    <td bgcolor=\\\"#ffffff\\\" style=\\\"background-color: #ffffff;\\\">\\n                        <table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" width=\\\"92%\\\" bgcolor=\\\"#ffffff\\\" align=\\\"center\\\" style=\\\"background-color: #ffffff; font-size: 13px;color:#333333;line-height: 18px;\\\">\\n                            <tr>\\n                                <td align=\\\"left\\\" bgcolor=\\\"#ffffff\\\" style=\\\"background-color: #ffffff;padding: 10px 0;\\\">\\n                                    <p style=\\\"margin: 10px 0;\\\">Bonjour Romain,<\\/p>\\n                                    <p style=\\\"margin: 10px 0 15px;\\\">Voici ce qui s'est passé dans <a target=\\\"_blank\\\" style=\\\"text-decoration: none; font-weight: bold; color: #2F5E92; \\\" href=\\\"http://localhost:8080/rest/social/notifications/redirectUrl/portal_home/eXo\\\">eXo<\\/a>&nbsp;<strong style=\\\"color: #333; font-weight: bold; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif; font-size: 13px; line-height: 18px;\\\">aujourd'hui<\\/strong>.\\n                                    <\\/p>\\n                                    \\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t   <ul style=\\\"list-style-type: none; padding-left: 0; color: #2F5E92;\\\"><li style=\\\"margin: 0; background-color: #F9F9F9; padding: 15px 20px; font-size: 13px; line-height: 18px; font-family: HelveticaNeue, Helvetica, Arial, sans-serif;\\\"><a target=\\\"_blank\\\" style=\\\"text-decoration: none; font-weight: bold; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif; font-size: 13px; line-height: 18px;\\\" href=\\\"http://localhost:8080/rest/social/notifications/redirectUrl/user/test32\\\">Francky Smithé<\\/a> <span style=\\\"color:#333333\\\"> a rejoint<\\/span> <a target=\\\"_blank\\\" style=\\\"text-decoration: none; font-weight: bold; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif; font-size: 13px; line-height: 18px;\\\" href=\\\"http://localhost:8080/rest/social/notifications/redirectUrl/portal_home/eXo\\\">eXo<\\/a><\\/li><\\/ul>\\n\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t\\t \\n                                    \\n                                    \\n                                    <p style=\\\"margin: 10px 0; color: #999999; font-family: HelveticaNeue, Helvetica, Arial, sans-serif;\\\">\\n                                        Si vous ne désirez pas recevoir ces notifications, <a style=\\\"text-decoration: none; color: #2F5E92; \\\" href=\\\"http://localhost:8080/rest/social/notifications/redirectUrl/notification_settings/jsmith\\\">cliquez ici<\\/a> pour modifier vos paramètres de notification.\\n                                    <\\/p>\\n                                <\\/td>\\n                            <\\/tr>\\n                        <\\/table>\\n                    <\\/td>\\n                <\\/tr>\\n            <\\/table>\\n        <\\/td>\\n    <\\/tr><!--end content area-->\\n    <tr>\\n        <td bgcolor=\\\"#456693\\\" align=\\\"center\\\"  style=\\\"border:1px solid #456693;\\\">\\n            <table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" width=\\\"92%\\\"  style=\\\"font-size: 13px;line-height: 18px;font-family:HelveticaNeue,verdana,arial,tahoma\\\">\\n                <tr>\\n                    <td align=\\\"left\\\" valign=\\\"top\\\" style=\\\"font-family: HelveticaNeue, Helvetica, Arial, sans-serif,serif;color:#ffffff;font-size:13px;\\\" >\\n                        <h3 style=\\\"text-align: center; margin: 0; padding: 10px 0;\\\">\\n                            <a target=\\\"_blank\\\" style=\\\"color: #ffffff; font-size: 13px;font-family:'HelveticaNeue Bold',arial,tahoma,serif; font-weight: bold; text-decoration: none;\\\" href=\\\"http://www.exoplatform.com/company/en/home\\\" title=\\\"eXo Platform\\\">eXoPlatform<\\/a>\\n                        <\\/h3>               \\n                    <\\/td>\\n                <\\/tr>\\n            <\\/table>\\n        <\\/td>\\n    <\\/tr><!--end footer area-->     \\n<\\/table>\",\"createdTime\":1462378684868,\"subject\":\"Vos notifications eXo pour aujourd'hui\",\"class\":\"class org.exoplatform.commons.api.notification.model.MessageInfo\",\"moveTop\":true,\"numberOnBadge\":0,\"from\":\"eXo<noreply@exoplatform.com>\"}";

    Method compress = QueueMessageImpl.class.getDeclaredMethod("compress", String.class);
    compress.setAccessible(true);
    InputStream is = (InputStream)compress.invoke(null, initialString);

    Method decompress = QueueMessageImpl.class.getDeclaredMethod("decompress", InputStream.class);
    decompress.setAccessible(true);
    String result = (String)decompress.invoke(null,is);

    assertTrue(result.equals(initialString));
    
  }
}
