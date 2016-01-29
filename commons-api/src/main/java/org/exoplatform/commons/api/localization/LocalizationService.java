package org.exoplatform.commons.api.localization;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.security.Identity;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Racha on 29/01/2016.
 */
public interface LocalizationService {

   public String getLanguage(ExoContainer container,Identity currentUser ,  HttpServletRequest request);
}
