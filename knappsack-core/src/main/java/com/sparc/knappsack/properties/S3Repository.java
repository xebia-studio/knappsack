package com.sparc.knappsack.properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.elasticache.AmazonElastiCacheClient;
import com.amazonaws.services.elasticache.model.CacheCluster;
import com.amazonaws.services.elasticache.model.CacheNode;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersRequest;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static com.sparc.knappsack.properties.SystemProperties.BUCKET_NAME;

public class S3Repository {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(S3Repository.class);

    private Properties properties;
    private String bucketName;
    private String fileKey;
    private String awsAccessKey;
    private String awsSecretKey;

    public String getProperty(String name) {
        if (properties == null) {
            init();
        }
        return properties.getProperty(name);
    }

    public S3Repository() {
        this.bucketName = System.getProperty(BUCKET_NAME);
        this.fileKey = System.getProperty(SystemProperties.PROPERTIES_FILE_KEY);
        this.awsAccessKey = System.getProperty(SystemProperties.KNAPPSACK_ACCESS_KEY);
        this.awsSecretKey = System.getProperty(SystemProperties.KNAPPSACK_SECRET_KEY);

        init();
    }

    public void init() {
        properties = new Properties();

        try {
            AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
            AmazonS3Client s3Client = new AmazonS3Client(credentials);

            GetObjectRequest request = new GetObjectRequest(bucketName, fileKey);
            S3Object s3Object = s3Client.getObject(request);

            properties.load(s3Object.getObjectContent());

            properties.put(SystemProperties.MEMCACHED_ENDPOINTS, getElastiCacheEndpoints(properties.getProperty(SystemProperties.CACHE_CLUSTER_ID)));

        } catch (Exception e) {
            log.info("Bucket Name: " + bucketName + " File Name: " + fileKey);
            log.info("Error reading properties from Amazon S3.", e);
        }
    }

    private String getElastiCacheEndpoints(String clusterId) {
        AmazonElastiCacheClient elastiCacheClient = new AmazonElastiCacheClient(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
        DescribeCacheClustersRequest request = new DescribeCacheClustersRequest();
        request.setCacheClusterId(clusterId);
        request.setShowCacheNodeInfo(true);
        DescribeCacheClustersResult result = elastiCacheClient.describeCacheClusters(request);
        List<String> endpoints = new ArrayList<String>();
        if (result != null) {
            for (CacheCluster cacheCluster : result.getCacheClusters()) {
                for (CacheNode node : cacheCluster.getCacheNodes()) {
                    endpoints.add(node.getEndpoint().getAddress() + ":" + node.getEndpoint().getPort());
                }
            }
        }

        return StringUtils.collectionToCommaDelimitedString(endpoints);
    }

}
