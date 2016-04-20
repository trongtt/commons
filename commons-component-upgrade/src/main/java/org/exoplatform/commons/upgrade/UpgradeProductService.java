package org.exoplatform.commons.upgrade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.picocontainer.Startable;

import org.exoplatform.commons.info.MissingProductInformationException;
import org.exoplatform.commons.info.ProductInformations;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class UpgradeProductService implements Startable {

  private static final Log                 LOG                           = ExoLogger.getLogger(UpgradeProductService.class);

  private static final String              PLUGINS_ORDER                 = "commons.upgrade.plugins.order";

  private static final String              PROCEED_UPGRADE_FIRST_RUN_KEY = "proceedUpgradeWhenFirstRun";

  private static final String              PRODUCT_VERSION_ZERO          = "0";

  private List<UpgradeProductPlugin>       upgradePlugins                = new ArrayList<UpgradeProductPlugin>();

  private Set<UpgradeProductPlugin>        allUpgradePlugins             = new TreeSet<UpgradeProductPlugin>();

  private ProductInformations              productInformations           = null;

  private boolean                          proceedUpgradeFirstRun        = false;

  private String                           pluginsOrder                  = null;

  private Comparator<UpgradeProductPlugin> pluginsComparator             = null;

  /**
   * Constructor called by eXo Kernel
   * 
   * @param productInformations
   */
  public UpgradeProductService(ProductInformations productInformations, InitParams initParams) {
    this.productInformations = productInformations;
    if (!initParams.containsKey(PROCEED_UPGRADE_FIRST_RUN_KEY)) {
      LOG.warn("init param '" + PROCEED_UPGRADE_FIRST_RUN_KEY + "' isn't set, use default value (" + proceedUpgradeFirstRun
          + "). Don't proceed upgrade when this service will run for the first time.");
    } else {
      proceedUpgradeFirstRun = Boolean.parseBoolean(initParams.getValueParam(PROCEED_UPGRADE_FIRST_RUN_KEY).getValue());
    }
    // Gets the execution order property:
    // "commons.upgrade.plugins.order".
    pluginsOrder = PropertyManager.getProperty(PLUGINS_ORDER);

    pluginsComparator = new Comparator<UpgradeProductPlugin>() {
      /**
       * {@inheritDoc}
       */
      @Override
      public int compare(UpgradeProductPlugin o1, UpgradeProductPlugin o2) {
        int index1 = pluginsOrder.indexOf(o1.getName());
        index1 = index1 < 0 ? upgradePlugins.size() : index1;
        int index2 = pluginsOrder.indexOf(o2.getName());
        index2 = index2 < 0 ? upgradePlugins.size() : index2;
        return index2 - index1;
      }
    };
  }

  /**
   * Method called by eXo Kernel to inject upgrade plugins
   * 
   * @param upgradeProductPlugin
   */
  public void addUpgradePlugin(UpgradeProductPlugin upgradeProductPlugin) {
    LOG.info("Add Product UpgradePlugin: name = " + upgradeProductPlugin.getName());
    if (upgradePlugins.contains(upgradeProductPlugin)) {
      LOG.warn(upgradeProductPlugin.getName() + " upgrade plugin is duplicated. One of the duplicated plugins will be ignored!");
    }
    // add only enabled plugins
    if (upgradeProductPlugin.isEnabled()) {
      upgradePlugins.add(upgradeProductPlugin);
      allUpgradePlugins.add(upgradeProductPlugin);
      // If the property does not exist, rely on the plugin execution
      // order: the order of the upgradeProductPlugin in upgradePlugins.
      if (!StringUtils.isBlank(pluginsOrder)) {
        Collections.sort(upgradePlugins, pluginsComparator);
      }
    } else {
      LOG.info("UpgradePlugin: name = '" + upgradeProductPlugin.getName() + "' will be ignored, because it is not enabled.");
    }
  }

  /**
   * This method is called by eXo Kernel when starting the parent ExoContainer
   */
  @Override
  public void start() {
    // Set previous version declaration to Zero,
    // this will force the upgrade execution on first run
    if (proceedUpgradeFirstRun) {
      productInformations.setPreviousVersionsIfFirstRun(PRODUCT_VERSION_ZERO);
      productInformations.storeProductsInformationsInJCR();
    }
    // If the upgradePluginNames array contains less elements than
    // the upgradePlugins list, execute these remaining plugins.
    for (int i = 0; i < upgradePlugins.size(); i++) {
      UpgradeProductPlugin upgradeProductPlugin = upgradePlugins.get(i);
      String previousUpgradePluginVersion = null;
      try {
        previousUpgradePluginVersion = productInformations.getPreviousVersion(upgradeProductPlugin.getName());
      } catch (MissingProductInformationException e) {
        try {
          previousUpgradePluginVersion = productInformations.getPreviousVersion(upgradeProductPlugin.getProductGroupId());
        } catch (MissingProductInformationException e1) {
          previousUpgradePluginVersion = PRODUCT_VERSION_ZERO;
        }
      }
      String currentUpgradePluginVersion = null;
      try {
        currentUpgradePluginVersion = productInformations.getVersion(upgradeProductPlugin.getName());
      } catch (MissingProductInformationException e) {
        try {
          currentUpgradePluginVersion = productInformations.getVersion(upgradeProductPlugin.getProductGroupId());
        } catch (MissingProductInformationException e1) {
          currentUpgradePluginVersion = PRODUCT_VERSION_ZERO;
        }
      }

      if (upgradeProductPlugin.shouldProceedToUpgrade(currentUpgradePluginVersion, previousUpgradePluginVersion)) {
        LOG.info("New version has been detected: proceed upgrading from " + previousUpgradePluginVersion + " to "
            + currentUpgradePluginVersion);
        // Product version has changed
        doUpgrade(upgradeProductPlugin, currentUpgradePluginVersion, previousUpgradePluginVersion, i);
      }
    }

    // The product has been upgraded, change the product version in the
    // JCR
    productInformations.storeProductsInformationsInJCR();

    LOG.info("Version upgrade completed.");
  }

  private void doUpgrade(UpgradeProductPlugin upgradeProductPlugin,
                         String currentUpgradePluginVersion,
                         String previousUpgradePluginVersion,
                         int order) {
    if (upgradeProductPlugin.shouldProceedToUpgrade(currentUpgradePluginVersion, previousUpgradePluginVersion)) {
      LOG.info("Proceed upgrade plugin: name = " + upgradeProductPlugin.getName() + " from version "
          + previousUpgradePluginVersion + " to " + currentUpgradePluginVersion + " with execution order = " + order);

      upgradeProductPlugin.processUpgrade(previousUpgradePluginVersion, currentUpgradePluginVersion);

      LOG.info("Upgrade " + upgradeProductPlugin.getName() + " completed.");
    } else {
      LOG.info("'" + upgradeProductPlugin.getName()
          + "' upgrade plugin execution will be ignored because shouldProceedToUpgrade = false");
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
  }

  /**
   * Re-import all upgrade-plugins for service
   */
  public void resetService() {
    // Reset product information
    productInformations.start();

    // Reload list Upgrade-Plugins
    upgradePlugins.clear();
    Iterator<UpgradeProductPlugin> iterator = allUpgradePlugins.iterator();
    while (iterator.hasNext()) {
      UpgradeProductPlugin upgradeProductPlugin = iterator.next();
      upgradePlugins.add(upgradeProductPlugin);
    }
  }

}
