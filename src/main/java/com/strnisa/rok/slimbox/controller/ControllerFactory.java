package com.strnisa.rok.slimbox.controller;

import com.strnisa.rok.slimbox.model.LiveModel;
import com.strnisa.rok.slimbox.model.Model;

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
