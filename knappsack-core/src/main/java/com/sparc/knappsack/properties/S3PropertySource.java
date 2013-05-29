package com.sparc.knappsack.properties;

import org.springframework.core.env.PropertySource;

public class S3PropertySource extends PropertySource<S3Repository> {

    public static final String S3_PROPERTY_SOURCE_NAME = "s3";

        private S3Repository repository;

        public S3PropertySource(String name, S3Repository source) {
            super(name, source);
            this.repository = source;
        }

        public S3PropertySource(S3Repository source) {
            this(S3_PROPERTY_SOURCE_NAME, source);
        }

        @Override
        public Object getProperty(String name) {
            try {
                return repository.getProperty(name);
            }
            catch (Exception e) {
                logger.error("Error accessing properties from S3: " + e.getMessage());
                return null;
            }
        }
}
