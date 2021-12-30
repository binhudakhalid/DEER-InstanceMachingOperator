package org.aksw.deer.plugin.kgfusion;

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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