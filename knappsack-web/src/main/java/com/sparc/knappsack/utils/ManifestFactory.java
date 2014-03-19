package com.sparc.knappsack.utils;

import com.google.common.io.Files;

import java.io.File;

public class ManifestFactory {

  private ManifestFactory() {

  }

  public static Manifest getInstance(File archive, String versionName) {
    if (Files.getFileExtension(archive.getName()).equals("apk")) {
      return new AndroidManifest(archive, versionName);
    } else if (Files.getFileExtension(archive.getName()).equals("ipa")) {
      return new IOSManifest(archive, versionName);
    } else {
      return new DefaultManifest(archive, versionName);
    }
  }

  public static class DefaultManifest extends Manifest {
    public DefaultManifest(File archive, String versionName) {
      super(archive, versionName);
    }

    @Override
    protected void parseManifestFile(File archive) {
      //do nothing
    }
  }


}
