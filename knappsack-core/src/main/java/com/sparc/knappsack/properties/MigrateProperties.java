package com.sparc.knappsack.properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.cloudformation.model.*;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

import static com.sparc.knappsack.properties.SystemProperties.*;

public class MigrateProperties {

    private static final Logger log = LoggerFactory.getLogger(MigrateProperties.class);

    private  AmazonCloudFormationClient amazonCloudFormationClient = null;

    private final String stackName;
    private final String bucketName;
    private final String fileKey;
    private final String awsAccessKey;
    private final String awsSecretKey;

    public MigrateProperties() {
        this.stackName = System.getProperty(STACK_NAME);
        this.bucketName = System.getProperty(BUCKET_NAME);
        this.fileKey = System.getProperty(PROPERTIES_FILE_KEY);
        this.awsAccessKey = System.getProperty(KNAPPSACK_ACCESS_KEY);
        this.awsSecretKey = System.getProperty(KNAPPSACK_SECRET_KEY);
        if(this.awsAccessKey != null && this.awsSecretKey != null) {
            AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
            amazonCloudFormationClient = new AmazonCloudFormationClient(awsCredentials);
        }
    }

    private boolean hasExistingProperties() {
        S3Service s3Service = getS3Service();
        boolean isObjectInBucket;
        try {
            isObjectInBucket = s3Service.isObjectInBucket(bucketName, fileKey);
        } catch (ServiceException e) {
            log.error("Error caught attempting to check if " + fileKey + " exists in bucket " + bucketName);
            return true;
        }

        return isObjectInBucket;
    }

    public void migrate() {
        log.info("Attempting to migrate properties from CloudFormation to knappsack.properties in S3 bucket " + bucketName);
        if(amazonCloudFormationClient == null) {
            return;
        }

        if(hasExistingProperties()) {
            log.info("Properties " + fileKey + " already exists.  Skipping migration.");
            return;
        }

        DescribeStacksRequest describeStackResourcesRequest = new DescribeStacksRequest();
        describeStackResourcesRequest.setStackName(stackName);
        DescribeStacksResult describeStackResult = amazonCloudFormationClient.describeStacks(describeStackResourcesRequest);

        Properties properties = getPropertiesFromStack(describeStackResult);

        ByteArrayOutputStream propertiesByteArrayOutputStream = new ByteArrayOutputStream();
        try {
            properties.store(propertiesByteArrayOutputStream, "Configuration properties for Knappsack based on the CloudFormation stack: " + stackName);
        } catch (IOException e) {
            log.error("Error storing properties to ByteArrayOutputStream", e);
            return;
        }

        S3Object objectToSave;
        try {
            objectToSave = new S3Object(fileKey, propertiesByteArrayOutputStream.toByteArray());
            objectToSave.setContentType("plain/text");
        } catch (NoSuchAlgorithmException e) {
            log.error("Error attempting to create S3 object " + fileKey + " for bucket " + bucketName, e);
            return;
        } catch (IOException e) {
            log.error("Error attempting to create S3 object " + fileKey + " for bucket " + bucketName, e);
            return;
        }

        S3Service s3Service = getS3Service();
        try {
            s3Service.putObject(bucketName, objectToSave);
        } catch (S3ServiceException e) {
            log.error("Error attempting to put S3 object " + fileKey + " in bucket " + bucketName, e);
        }

    }

    private Properties getPropertiesFromStack(DescribeStacksResult describeStackResult) {
        assert(describeStackResult != null);

        Properties properties = new Properties();
        Stack stack = describeStackResult.getStacks().get(0);
        if(stack == null) {
            return properties;
        }

        List<Parameter> parameters = stack.getParameters();
        for (Parameter parameter : parameters) {
            String paramKey = parameter.getParameterKey();
            properties.setProperty(paramKey, parameter.getParameterValue());

            if(SPRING_PROFILES_ACTIVE_CF.equals(paramKey)) {

                properties.setProperty(SPRING_PROFILES_ACTIVE, parameter.getParameterValue());
            }

            if(DYNAMODB_BANDWIDTH_TABLE.equals(paramKey)) {
                properties.setProperty(DYNAMODB_BANDWIDTH_TABLE, getDynamoDBTableNameFromARN(parameter.getParameterValue()));
            }

            if (SQS_RESIGN_QUEUE.equals(paramKey)) {
                properties.setProperty(SQS_RESIGN_QUEUE, getSqsResignQueueNameFromARN(parameter.getParameterValue()));
            }

            if (AWS_ENVIRONMENT.equals(paramKey)) {
                properties.setProperty(AWS_ENVIRONMENT, parameter.getParameterValue());
            }
        }

        List<Output> outputs = stack.getOutputs();
        for (Output output : outputs) {
            String outputKey = output.getOutputKey();
            if(EB_SECURITY_GROUP.equals(outputKey) ||
                    SQS_EMAIL_QUEUE.equals(outputKey) ||
                    CLOUDFRONT_URL.equals(outputKey) ||
                    CACHE_CLUSTER_ID.equals(outputKey)) {
                properties.setProperty(output.getOutputKey(), output.getOutputValue());
            }
        }

        return properties;
    }

    private S3Service getS3Service() {
        org.jets3t.service.security.AWSCredentials awsCredentials = new org.jets3t.service.security.AWSCredentials(awsAccessKey, awsSecretKey);
        S3Service s3Service;
        try {
            s3Service = new RestS3Service(awsCredentials);
        } catch (S3ServiceException e) {
            log.error("S3ServiceException attempting to create RestS3Service", e);
            return null;
        }

        return s3Service;
    }

    private String getDynamoDBTableNameFromARN(String dynamoDBARN) {
        if(dynamoDBARN == null || dynamoDBARN.isEmpty()) {
            return dynamoDBARN;
        }

        return dynamoDBARN.substring(dynamoDBARN.indexOf(":table/")+7);
    }

    private String getSqsResignQueueNameFromARN(String sqsResignQueueARN) {
        if (!StringUtils.hasText(sqsResignQueueARN)) {
            return sqsResignQueueARN;
        }

        String[] splitStrings = sqsResignQueueARN.split(":");
        return splitStrings[splitStrings.length-1];
    }
}
