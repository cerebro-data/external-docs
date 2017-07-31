package com.cerebro.benchmark;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3List {
  private static final AmazonS3 S3_CLIENT =
      new AmazonS3Client(new DefaultAWSCredentialsProviderChain());

  private static int totalFiles(Map<String, List<String>> perPartionFiles) {
    int c = 0;
    for (Map.Entry<String, List<String>> kv: perPartionFiles.entrySet()) {
      c += kv.getValue().size();
    }
    return c;
  }

  private static void awsListKeys(String path,
      Map<String, List<String>> fileDescMap) throws IOException {
    try {
      AmazonS3URI uri = new AmazonS3URI(path.replace("s3a://", "s3://"));
      final ListObjectsV2Request req = new ListObjectsV2Request()
          .withBucketName(uri.getBucket())
          .withPrefix(uri.getKey());
      ListObjectsV2Result result;
      String prefix = "s3://" + uri.getBucket() + "/";
      do {
         result = S3_CLIENT.listObjectsV2(req);
         for (S3ObjectSummary objectSummary: result.getObjectSummaries()) {
           String fullPath = prefix + objectSummary.getKey();
           String partitionDir = fullPath.substring(0, fullPath.lastIndexOf('/'));
           if (!fileDescMap.containsKey(partitionDir)) {
             fileDescMap.put(partitionDir, new ArrayList<String>());
           }
           fileDescMap.get(partitionDir).add(fullPath);
         }
         req.setContinuationToken(result.getNextContinuationToken());
      } while (result.isTruncated());
     } catch (AmazonServiceException ase) {
       throw new IOException("Request rejected by S3: " + ase.getMessage());
    } catch (AmazonClientException ace) {
      throw new IOException("Client error reaching S3: " + ace.getMessage());
    }
  }

  private static void measureLoadBatchedAws(String root)
      throws FileNotFoundException, IllegalArgumentException, IOException {
    Map<String, List<String>> partitionMap = new HashMap<String, List<String>>();
    long start = System.currentTimeMillis();
    awsListKeys(root, partitionMap);
    long elapsedMs = System.currentTimeMillis() - start;
    int totalFiles = totalFiles(partitionMap);
    System.out.println("#Objects: " + totalFiles(partitionMap));
    System.out.println("#Partitions: " + partitionMap.size());
    System.out.println("AwsListKeys(): " + elapsedMs + "ms");
    System.out.println("Objects/second: " + (totalFiles * 1000 / elapsedMs));
    System.out.println("");
  }

  public static final void main(String[] args)
      throws FileNotFoundException, IllegalArgumentException, IOException {
    int numIters = 1;

    if (args.length == 0) {
      System.err.println(
          "Usage: java -cp <PATH TO JAR> com.cerebro.benchmark.S3List <S3 Directory>] [NUM_ITERS]");
      System.exit(1);
    }
    String root = args[0];
    if (args.length > 1) numIters = Integer.parseInt(args[1]);

    for (int i = 0; i < numIters; i++) {
      System.out.println("Running iteration: " + (i + 1));
      measureLoadBatchedAws(root);
    }
  }
}