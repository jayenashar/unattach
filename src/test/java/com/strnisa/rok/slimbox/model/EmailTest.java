package com.strnisa.rok.slimbox.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailTest {
  private Email getEmail(String from) {
    return new Email("id3", "uid42", null, from, "subject", 1501545600000L,
        32141);
  }

  @Test
  void getFromEmail() {
    assertEquals("rok@strnisa.com", getEmail("rok@strnisa.com").getFromEmail());
    assertEquals("rok@strnisa.com", getEmail("Rok Strniša <rok@strnisa.com>").getFromEmail());
    assertEquals("rok@strnisa.com", getEmail("\"Rok Strniša\" <rok@strnisa.com>").getFromEmail());
  }

  @Test
  void getFromName() {
    assertEquals("_", getEmail("rok@strnisa.com").getFromName());
    assertEquals("Rok Strniša", getEmail("Rok Strniša <rok@strnisa.com>").getFromName());
    assertEquals("Rok Strniša", getEmail("\"Rok Strniša\" <rok@strnisa.com>").getFromName());
  }
}