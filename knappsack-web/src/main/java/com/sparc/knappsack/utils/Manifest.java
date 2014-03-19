package com.sparc.knappsack.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public abstract class Manifest {

  protected static final Logger log = LoggerFactory.getLogger(Manifest.class);
  protected String versionName = "";
  protected String label = "";

  public Manifest(File archive, String versionName) {
    this.versionName = versionName;
    parseManifestFile(archive);
  }

  protected abstract void parseManifestFile(File archive);

  public String getVersionName() {
    return versionName;
  }

  public String getLabel() {
    return label;
  }
}
