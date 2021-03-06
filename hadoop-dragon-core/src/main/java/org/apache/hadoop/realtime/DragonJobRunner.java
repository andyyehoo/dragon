/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 */
package org.apache.hadoop.realtime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.UnsupportedFileSystemException;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.realtime.client.ClientCache;
import org.apache.hadoop.realtime.protocol.records.GetJobReportRequest;
import org.apache.hadoop.realtime.protocol.records.GetJobReportResponse;
import org.apache.hadoop.realtime.records.JobId;
import org.apache.hadoop.realtime.records.JobReport;
import org.apache.hadoop.realtime.records.JobState;
import org.apache.hadoop.realtime.records.TaskAttemptId;
import org.apache.hadoop.realtime.records.TaskReport;
import org.apache.hadoop.realtime.security.token.delegation.DelegationTokenIdentifier;
import org.apache.hadoop.security.Credentials;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authorize.AccessControlList;
import org.apache.hadoop.security.token.Token;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.yarn.YarnException;
import org.apache.hadoop.yarn.api.records.ApplicationAccessType;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnRemoteException;
import org.apache.hadoop.yarn.util.BuilderUtils;

import com.google.inject.spi.TypeConverter;

public class DragonJobRunner implements DragonJobService {
  private static final Log LOG = LogFactory.getLog(DragonJobRunner.class);
  private ResourceMgrDelegate resMgrDelegate;
  private ClientCache clientCache;
  private Configuration conf;
  private final FileContext defaultFileContext;

  /**
   * Dragon runner incapsulates the client interface of yarn
   * 
   * @param conf the configuration object for the client
   */
  public DragonJobRunner(Configuration conf) {
    this(conf, new ResourceMgrDelegate(new YarnConfiguration(conf)));
  }

  /**
   * Similar to {@link #DragonRunner(Configuration)} but allowing injecting
   * {@link ResourceMgrDelegate}. Enables mocking and testing.
   * 
   * @param conf the configuration object for the client
   * @param resMgrDelegate the resourcemanager client handle.
   */
  public DragonJobRunner(Configuration conf, ResourceMgrDelegate resMgrDelegate) {
    this.conf = conf;
    try {
      this.resMgrDelegate = resMgrDelegate;
      this.defaultFileContext = FileContext.getFileContext(this.conf);
    } catch (UnsupportedFileSystemException ufe) {
      throw new RuntimeException("Error in instantiating YarnClient", ufe);
    }
  }

  @Override
  public JobId getNewJobId() throws IOException, InterruptedException {
    return resMgrDelegate.getNewJobID();
  }

  @Override
  public boolean submitJob(JobId jobId, String jobSubmitDir, Credentials ts)
      throws IOException, InterruptedException {
    // Get the security token of the jobSubmitDir and store in Credentials
    Path applicationTokensFile =
        new Path(jobSubmitDir, DragonJobConfig.APPLICATION_TOKENS_FILE);
    try {
      ts.writeTokenStorageFile(applicationTokensFile, conf);
    } catch (IOException e) {
      throw new YarnException(e);
    }
    // Construct necessary information to start the Dragon AM
    ApplicationSubmissionContext appContext =
        createApplicationSubmissionContext(jobSubmitDir, ts);
    // Submit to ResourceManager
    ApplicationId applicationId = resMgrDelegate.submitApplication(appContext);
    ApplicationReport appMaster =
        resMgrDelegate.getApplicationReport(applicationId);
    String diagnostics =
        (appMaster == null ? "application report is null" : appMaster
            .getDiagnostics());
    if (appMaster == null
        || appMaster.getYarnApplicationState() == YarnApplicationState.FAILED
        || appMaster.getYarnApplicationState() == YarnApplicationState.KILLED) {
      throw new IOException("Failed to run job : " + diagnostics);
    }
    return true;
  }

  @Override
  public String getSystemDir() throws IOException, InterruptedException {
    return resMgrDelegate.getSystemDir();
  }

  @Override
  public String getStagingAreaDir() throws IOException, InterruptedException {
    return resMgrDelegate.getStagingAreaDir();
  }

  public ApplicationSubmissionContext createApplicationSubmissionContext(
      String jobSubmitDir, Credentials ts) throws IOException {
    ApplicationId applicationId = resMgrDelegate.getApplicationId();

    // Setup capability
    Resource capability = DragonApps.setupResources(conf);

    // Setup LocalResources
    Map<String, LocalResource> localResources =
        new HashMap<String, LocalResource>();

    // JonConf
    Path jobConfPath = new Path(jobSubmitDir, DragonJobConfig.JOB_CONF_FILE);
    localResources.put(DragonJobConfig.JOB_CONF_FILE,
        DragonApps.createApplicationResource(defaultFileContext, jobConfPath));

    // JobJar
    if (conf.get(DragonJobConfig.JAR) != null) {
      localResources.put(DragonJobConfig.JOB_JAR, DragonApps
          .createApplicationResource(defaultFileContext, new Path(jobSubmitDir,
              DragonJobConfig.JOB_JAR)));
    } else {
      LOG.info("Job jar is not present. "
          + "Not adding any jar to the list of resources.");
    }

    // Setup security tokens
    ByteBuffer securityTokens = null;
    if (UserGroupInformation.isSecurityEnabled()) {
      DataOutputBuffer dob = new DataOutputBuffer();
      ts.writeTokenStorageToStream(dob);
      securityTokens = ByteBuffer.wrap(dob.getData(), 0, dob.getLength());
    }
    // Setup the command to run the AM
    List<String> vargs = DragonApps.setupCommands(conf);

    // Setup environment
    Map<String, String> environment = new HashMap<String, String>();

    // Setup the CLASSPATH in environment
    DragonApps.setClasspath(environment, conf);

    // Setup the ACLs
    Map<ApplicationAccessType, String> acls = DragonApps.setupACLs(conf);

    // Generate ContainerLaunchContext for AM container
    ContainerLaunchContext amContainer =
        BuilderUtils.newContainerLaunchContext(null, UserGroupInformation
            .getCurrentUser().getShortUserName(), capability, localResources,
            environment, vargs, null, securityTokens, acls);

    return DragonApps.newApplicationSubmissionContext(applicationId, conf,
        amContainer);
  }

  @Override
  public AccessControlList getQueueAdmins(String queueName) throws IOException {
    return null;
  }

  public Token<DelegationTokenIdentifier> getDelegationToken(Text renewer)
      throws IOException, InterruptedException {
    // The token is only used for serialization. So the type information
    // mismatch should be fine.
    return resMgrDelegate.getDelegationToken(renewer);
  }

  @Override
  public void killJob(JobId jobId) throws IOException, InterruptedException {
    /* check if the status is not running, if not send kill to RM */
    JobState state = clientCache.getClient(jobId).getJobReport(jobId).getJobState();
    if (state != JobState.RUNNING) {
      resMgrDelegate.killApplication(jobId.getAppId());
      return;
    }
    try {
      /* send a kill to the AM */
      clientCache.getClient(jobId).killJob(jobId);
      long currentTimeMillis = System.currentTimeMillis();
      long timeKillIssued = currentTimeMillis;
      while ((currentTimeMillis < timeKillIssued + 10000L) && (state
          != JobState.KILLED)) {
          try {
            Thread.sleep(1000L);
          } catch(InterruptedException ie) {
            /** interrupted, just break */
            break;
          }
          currentTimeMillis = System.currentTimeMillis();
          state = clientCache.getClient(jobId).getJobReport(jobId).getJobState();
      }
    } catch(IOException io) {
      LOG.debug("Error when checking for application status", io);
    }
    if (state != JobState.KILLED) {
      resMgrDelegate.killApplication(jobId.getAppId());
    }
  }

  @Override
  public boolean killTask(TaskAttemptId taskId, boolean shouldFail)
      throws IOException, InterruptedException {
    clientCache.getClient(taskId.getTaskId().getJobId()).killTask(taskId,
        shouldFail);
    return false;
  }

  @Override
  public List<TaskReport> getTaskReports(JobId jobId) throws IOException,
      InterruptedException {
    return clientCache.getClient(jobId).getTaskReports(jobId);
  }


  @Override
  public JobReport getJobReport(JobId jobId) throws YarnRemoteException {
    return clientCache.getClient(jobId).getJobReport(jobId);
  }
}
