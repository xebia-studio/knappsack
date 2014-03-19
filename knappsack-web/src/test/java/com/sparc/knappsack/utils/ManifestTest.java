package com.sparc.knappsack.utils;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class ManifestTest {


  @Test
  public void shouldReadAndroidManifestXmlFile() throws URISyntaxException, IOException {
    File app = new File(getClass().getResource("/turf-android-1.1.2-rct.apk").toURI());
    Manifest manifest = ManifestFactory.getInstance(app, null);

    assertEquals("1.1.2", manifest.getVersionName());
    assertEquals("Turf QPlusTLJ rct", manifest.getLabel());
  }

  @Test
  public void shouldReadIOSManifestPlistFile() throws URISyntaxException {
    File app = new File(getClass().getResource("/SoMusic-0.4-resigned.ipa").toURI());
    Manifest manifest = ManifestFactory.getInstance(app, "98");

    assertEquals("1.0", manifest.getVersionName());
    assertEquals("SoMusic", manifest.getLabel());
  }

  @Test
  public void shouldInstantiateManifestWithDefaultValue() throws URISyntaxException {
    Manifest manifest = ManifestFactory.getInstance(new File("test"), "98");

    assertEquals("98", manifest.getVersionName());
    assertEquals("", manifest.getLabel());
  }
}
