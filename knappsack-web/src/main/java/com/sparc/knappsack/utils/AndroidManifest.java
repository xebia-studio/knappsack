package com.sparc.knappsack.utils;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AndroidManifest extends Manifest {


  public static final String ANDROID_MANIFEST_XML = "AndroidManifest.xml";

  public AndroidManifest(File archive, String versionName) {
    super(archive, versionName);
  }

  protected void parseManifestFile(File archive) {
    AndroidManifestBinaryReader binaryReader = new AndroidManifestBinaryReader();
    try {
      ZipFile zipFile = new ZipFile(archive);
      ZipEntry entry = zipFile.getEntry(ANDROID_MANIFEST_XML);
      if (entry != null) {
        byte[] xml = new byte[zipFile.getInputStream(entry).available()];
        zipFile.getInputStream(entry).read(xml);
        parseXml(binaryReader.decompressXML(xml));
      }
    } catch (IOException e) {
      log.warn("cannot read archive from app");
    } catch (XMLStreamException e) {
      log.warn("cannot parse file from app manifest");
    }
  }

  protected void parseXml(String manifest) throws XMLStreamException {
    XMLInputFactory xmlif = XMLInputFactory.newInstance();
    XMLStreamReader xmlsr = xmlif.createXMLStreamReader(new StringReader(manifest));
    int eventType;
    while (xmlsr.hasNext()) {
      eventType = xmlsr.next();
      switch (eventType) {
        case XMLEvent.START_ELEMENT:
          if (xmlsr.getLocalName().equals("manifest")) {
            versionName = xmlsr.getAttributeValue("", "versionName");
          } else if (xmlsr.getName().toString().equals("application")) {
            label = xmlsr.getAttributeValue("", "label");
          }
          break;
        default:
          break;
      }
    }
  }


}
