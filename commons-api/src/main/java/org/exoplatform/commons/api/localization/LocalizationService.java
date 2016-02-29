package org.exoplatform.commons.api.localization;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.security.Identity;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by Racha on 29/01/2016.
 */
public interface LocalizationService {

   public Locale getLocale(ExoContainer container, Identity currentUser, HttpServletRequest request);
}
