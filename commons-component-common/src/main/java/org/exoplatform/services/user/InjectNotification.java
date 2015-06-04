/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.user;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.rest.resource.ResourceContainer;

import com.google.caja.util.Lists;



@Path("inject")
public class InjectNotification implements ResourceContainer {
  private static final Log LOG = ExoLogger.getLogger(InjectNotification.class);

  private final WebNotificationStorage webService;
  private final OrganizationService organizationService;

  public final static ArgumentLiteral<String> REMOTE_ID = new ArgumentLiteral<String>(String.class, "remoteId");

  public InjectNotification(WebNotificationStorage webService, OrganizationService organizationService) {
    this.webService = webService;
    this.organizationService = organizationService;
  }

  protected NotificationInfo makeWebNotificationInfo(String userId, String newUserId) {
    NotificationInfo info = NotificationInfo.instance()
                                            .key("NewUserPlugin")
                                            .with(REMOTE_ID.getKey(), newUserId)
                                            .with("ntf:read", "false")
                                            .with("ntf:showPopover", "true")
                                            .setSendAll(true)
                                            .setFrom(newUserId)
                                            .setTo(userId)
                                            .end();
    return info;
  }
    
  /**
   * The rest suppor to inject notifications with type NewUserPlugin
   * The URL: http://{domain}/rest/inject/notifications?number=N&users=U&from=X&to=Y
   * 
   * @param numberperday - The number of notifications create on each day of each user
   * @param numberUsers - The users load from {@link OrganizationService}
   * @param fromday - The number from X days ago
   * @param today - The number to Y days ago
   *        Note: Y need more than X (example: from 30 days ago to 35 days ago)
   * @return Result OK when done injects
   * @throws Exception 
   */
  @GET
  @Path("notifications")
  @RolesAllowed("users")
  @Produces(MediaType.TEXT_HTML)
  public Response injectNotification(@QueryParam("number") String numberperday,
                                     @QueryParam("users") String numberUsers,
                                     @QueryParam("from") String fromday,
                                     @QueryParam("to") String today) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(false);
    int number = getInterger(numberperday),
        users = getInterger(numberUsers),
        from = getInterger(fromday),
        to = getInterger(today);
    if (number == 0 || users == 0 || from == 0 || from > to) {
      return Response.ok("Error: the value is invalid").cacheControl(cacheControl).build();
    }
    LOG.info(String.format("Stating to inject %s notifications for each day for each user. with %s users and from %s days ago to %s days ago.",
                           number, users, from, to));
    //
    String newUserId = "root";
    Calendar cal = Calendar.getInstance();
    long timeOfDay = 86400000l, current = cal.getTimeInMillis();
    int count = 0;
    //
    ListAccess<User> userListAccess = organizationService.getUserHandler().findAllUsers();
    List<User> loadUser = Lists.newArrayList(userListAccess.load(0, users));
    Random random = new Random();
    //
    for (User user : loadUser) {// for user list
      LOG.info(String.format("Stating to inject for user %s", user.getUserName()));
      for (int d = from; d < to; ++d) { // for days
        cal.setTimeInMillis(current - d * timeOfDay);
        for (int j = 0; j < number; ++j) { // for notifications each day.
          int index = random.nextInt(users) - 1;
          newUserId = loadUser.get((index < 0) ? 0 : index).getUserName();
          NotificationInfo info = makeWebNotificationInfo(user.getUserName(), newUserId).setDateCreated(cal);
          //
          webService.save(info);
          count++;
        }
      }
    }
    String result = String.format("Done for inject %s notifications and consume time: %s(ms)", count, (System.currentTimeMillis() - current));
    LOG.info(result);
    
    return Response.ok(result).cacheControl(cacheControl).build();
  }

  private int getInterger(String value) {
    try {
      return Integer.valueOf(value.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }
}
