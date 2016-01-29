package org.exoplatform.commons.notification.impl.localization;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.Constants;
import org.exoplatform.portal.application.localization.LocalizationLifecycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.LocalePolicy;
import org.exoplatform.services.security.Identity;
import org.exoplatform.commons.api.localization.LocalizationService;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Racha on 29/01/2016.
 */
public class LocalizationServiceImp implements LocalizationService {


    private static final Log LOG = ExoLogger.getLogger(LocalizationServiceImp.class);
    private String lang = null;

    @Override
   public String getLanguage(ExoContainer container,Identity currentUser,  HttpServletRequest request ){
        try {
            LocaleConfigService localeConfigService = (LocaleConfigService) container.getComponentInstanceOfType(LocaleConfigService.class);
            LocalePolicy localePolicy = (LocalePolicy) container.getComponentInstanceOfType(LocalePolicy.class);
            LocaleContextInfo localeCtx = new LocaleContextInfo();
            Locale portalLocale = Locale.getDefault();
            Set<Locale> supportedLocales = new HashSet();

            for (LocaleConfig lc : localeConfigService.getLocalConfigs()) {
                supportedLocales.add(lc.getLocale());
            }

            localeCtx.setSupportedLocales(supportedLocales);
            localeCtx.setBrowserLocales(Collections.list(request.getLocales()));
            localeCtx.setCookieLocales(LocalizationLifecycle.getCookieLocales(request));
            localeCtx.setSessionLocale(LocalizationLifecycle.getSessionLocale(request));
            localeCtx.setUserProfileLocale(getUserProfileLocale(container, currentUser.getUserId()));
            localeCtx.setRemoteUser(currentUser.getUserId());

            localeCtx.setPortalLocale(checkPortalLocaleSupported(portalLocale, supportedLocales));
            Locale locale = localePolicy.determineLocale(localeCtx);
            lang = locale.getLanguage();

        } catch (Exception e) {
            LOG.error("Cannot get the current language of the user " + currentUser.getUserId());
        } finally {
            return  lang;
        }

    }


    private Locale checkPortalLocaleSupported(Locale portalLocale, Set<Locale> supportedLocales) {

        if (supportedLocales.contains(portalLocale))
            return portalLocale;
        if ("".equals(portalLocale.getCountry()) == false) {
            Locale loc = new Locale(portalLocale.getLanguage());
            if (supportedLocales.contains(loc)) {
                LOG.warn("portalLocale not supported: " + LocaleContextInfo.getLocaleAsString(portalLocale)
                        + ". Falling back to '" + portalLocale.getLanguage() + "'.");
                portalLocale = loc;
                return loc;
            }
        }

        LOG.warn("portalLocale not supported: " + LocaleContextInfo.getLocaleAsString(portalLocale)
                + ". Falling back to Locale.ENGLISH.");
        portalLocale = Locale.ENGLISH;
        return portalLocale;
    }

    private Locale getUserProfileLocale( ExoContainer container, String user) throws Exception {

        UserProfile userProfile = null;
        OrganizationService svc = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
        if (user != null) {
            try {
                beginContext(svc);
                userProfile = svc.getUserProfileHandler().findUserProfileByName(user);
            } catch (Exception ignored) {
                LOG.error("IGNORED: Failed to load UserProfile for username: " + user, ignored);
            } finally {
                try {
                    endContext(svc);
                } catch (Exception ignored) {
                    // we don't care
                }
            }

            if (userProfile == null && LOG.isWarnEnabled())
                LOG.warn("Could not load user profile for " + user);
        }
        String lang = userProfile == null ? null : userProfile.getUserInfoMap().get(Constants.USER_LANGUAGE);
        return (lang != null) ? LocaleContextInfo.getLocale(lang) : null;
    }

    private void beginContext(OrganizationService orgService) {
        if (orgService instanceof ComponentRequestLifecycle) {
            RequestLifeCycle.begin((ComponentRequestLifecycle) orgService);
        }
    }

    private void endContext(OrganizationService orgService) {
        // do the same check as in beginContext to make it symmetric
        if (orgService instanceof ComponentRequestLifecycle) {
            RequestLifeCycle.end();
        }
    }
}
