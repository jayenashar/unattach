package app.unattach.controller;

import app.unattach.model.LiveModel;
import app.unattach.model.Model;

public class ControllerFactory {
  private static Controller defaultController;

  public static synchronized Controller getDefaultController() {
    if (defaultController == null) {
      Model model = new LiveModel();
//      Model model = new MockModel();
      defaultController = new DefaultController(model);
    }
    return defaultController;
  }
}
