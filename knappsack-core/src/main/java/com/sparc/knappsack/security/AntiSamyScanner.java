package com.sparc.knappsack.security;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.Policy;
import org.owasp.validator.html.PolicyException;
import org.owasp.validator.html.ScanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class AntiSamyScanner {

    private static final Logger log = LoggerFactory.getLogger(AntiSamyScanner.class);

    private static final String POLICY = "security/antisamy.xml";
    private static Policy policy;

    static {
        try {
            URL resourceURL = AntiSamyScanner.class.getClassLoader().getResource(POLICY);
            if (resourceURL != null) {
                InputStream is = resourceURL.openStream();
                policy = Policy.getInstance(is);
            }
        } catch (PolicyException e) {
            log.error("PolicyException caught creating OWASP policy for file: " + POLICY, e);
        } catch (IOException e) {
            log.error("IOException caught getting file from resource: " + POLICY, e);
        }
    }

    public static String cleanHTML(String value) {
        try {
            return getAntiSamy().scan(value).getCleanHTML();
        } catch (ScanException e) {
            log.error("ScanException caught scanning String with value: " + value, e);
            return value;
        } catch (PolicyException e) {
            log.error("PolicyException caught scanning String with value: " + value, e);
            return value;
        }
    }

    public static AntiSamy getAntiSamy() {
        return new AntiSamy(policy);
    }
}
