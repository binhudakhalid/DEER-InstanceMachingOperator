package org.aksw.deer.plugin.kgfusion;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This plugin wrapper for InstanceMatchingOperator class
 * 
 * @author Khalid Bin Huda Siddiqui (khalids@campus.uni-paderborn.de)
 * @author Khalid Khan (kkhan@campus.uni-paderborn.de)
 */
public class InstanceMatchingPlugin extends Plugin {

  private static final Logger logger = LoggerFactory.getLogger(InstanceMatchingPlugin.class);

  public InstanceMatchingPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void start() {
    logger.info("WelcomePlugin.start()");
  }

  @Override
  public void stop() {
    logger.info("WelcomePlugin.stop()");
  }

}