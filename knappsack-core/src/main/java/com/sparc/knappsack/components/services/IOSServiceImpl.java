package com.sparc.knappsack.components.services;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.sparc.knappsack.components.entities.AppFile;
import com.sparc.knappsack.components.entities.ApplicationVersion;
import com.sparc.knappsack.util.WebRequest;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("iosService")
public class IOSServiceImpl implements IOSService {

    @Qualifier("applicationVersionService")
    @Autowired(required = true)
    private ApplicationVersionService applicationVersionService;

    @Qualifier("storageServiceFactory")
    @Autowired(required = true)
    private StorageServiceFactory storageServiceFactory;

    @Override
    public String createIOSPlistXML(Long applicationVersionId, WebRequest webRequest, String token) {
        ApplicationVersion applicationVersion = applicationVersionService.get(applicationVersionId);

        if (applicationVersion == null) {
            return null;
        }

        String downloadUrl = createIOSDownloadIPAUrl(applicationVersion, webRequest, token);

        NSDictionary nsDictionary = createIOSPlistNSDictionary(applicationVersion.getCfBundleIdentifier(), applicationVersion.getCfBundleVersion(), applicationVersion.getCfBundleName(), downloadUrl);

        return nsDictionary.toXMLPropertyList();
    }

    private String createIOSDownloadIPAUrl(ApplicationVersion version, WebRequest request, String token) {
        AppFile appFile = version.getInstallationFile();

        StorageService storageService = storageServiceFactory.getStorageService(appFile.getStorageType());

        NameValuePair tokenParam = new BasicNameValuePair("token", token);
        return request.generateURL("/ios/downloadApplication/" + version.getId(), tokenParam);

    }

    private NSDictionary createIOSPlistNSDictionary(String cfBundleIdentifier, String cfBundleVersion, String cfBundleName, String ipaUrl) {
        NSDictionary mainDict = new NSDictionary();

        NSArray items = new NSArray(1);
        NSArray assets = new NSArray(1);
        NSDictionary itemsDict = new NSDictionary();

        NSDictionary assetsDict = new NSDictionary();
        assetsDict.put("kind", "software-package");
        assetsDict.put("url", ipaUrl);

        assets.setValue(0, assetsDict);

        NSDictionary mettadataDict = new NSDictionary();
        mettadataDict.put("bundle-identifier", cfBundleIdentifier);
        mettadataDict.put("bundle-version", cfBundleVersion);
        mettadataDict.put("kind", "software");
        mettadataDict.put("title", cfBundleName);

        itemsDict.put("assets", assets);
        itemsDict.put("metadata", mettadataDict);

        items.setValue(0, itemsDict);

        mainDict.put("items", items);

        return mainDict;
    }
}
