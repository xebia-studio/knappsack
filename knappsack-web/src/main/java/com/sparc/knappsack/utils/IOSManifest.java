package com.sparc.knappsack.utils;

import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class IOSManifest extends Manifest {


  public static final String IOS_MANIFEST_NAME = "Info.plist";

  public IOSManifest(File archive, String versionName) {
    super(archive, versionName);
  }

  protected void parseManifestFile(File archive) {
    try {
      ZipFile zip = new ZipFile(archive);
      Enumeration<? extends ZipEntry> zipEntries = zip.entries();
      String plistPath = "";
      for (int i = 0; zip.entries().hasMoreElements(); i++) {
        ZipEntry entry = zipEntries.nextElement();
        if (i == 1) {
          plistPath = entry.getName() + IOS_MANIFEST_NAME;
        }
        if (entry.getName().equals(plistPath)) {
          parseXml(zip.getInputStream(entry));
          break;
        }
      }
    } catch (IOException e) {
      log.warn("cannot read archive from app");
    }
  }

  protected void parseXml(InputStream manifest) throws IOException {
    NSDictionary rootDict;
    try {
      rootDict = (NSDictionary) PropertyListParser.parse(manifest);
      versionName = rootDict.get("CFBundleShortVersionString").toString();
      label = rootDict.get("CFBundleName").toString();
    } catch (PropertyListFormatException e) {
      log.warn("cannot parse file from app manifest");
    } catch (ParseException e) {
      log.warn("cannot parse file from app manifest");
    } catch (ParserConfigurationException e) {
      log.warn("cannot parse file from app manifest");
    } catch (SAXException e) {
      log.warn("cannot parse file from app manifest");
    }

  }


}
