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
package org.apache.hadoop.realtime;

public class DragonJobConfig {
  
  public static final String ID = "dragon.job.id";

  public static final String JOB_NAME = "dragon.job.name";
  
  // This should be the name of the localized job-jar file on the node running
  // individual containers/tasks.
  public static final String JOB_JAR = "graphjob.jar";

	// Put all of the attribute names in here so that Job and JobContext are
	// consistent.
	public static final String JAR = "dragon.job.jar";

  public static final String JOB_SUBMIT_FILE_REPLICATION = 
      "dragon.client.submit.file.replication";

  public static final String CHILD_JAVA_OPTS =
      "dragon.child.java.opts";
  
  // This should be the name of the localized job-configuration file on the node
  // running ApplicationMaster and Task
  public static final String JOB_CONF_FILE = "graphjob.xml";

	public static final String USER_NAME = "dragon.job.user.name";

	public static final String PRIORITY = "dragon.job.priority";


  /* Entry class for child processes on each node */
  public static final String CHILD_CLASS =
      "org.apache.hadoop.realtime.DragonChild";
  
  public static final String JOB_SUBMITHOST =
      "dragon.job.submithostname";
  public static final String JOB_SUBMITHOSTADDR =
      "dragon.job.submithostaddress";

  public static final String JOB_NAMENODES = "dragon.job.hdfs-servers";

  /*
   * config for tracking the local file where all the credentials for the job
   * credentials.
   */
  public static final String DRAGON_JOB_CREDENTIALS_BINARY =
      "dragon.job.credentials.binary";
  
  public static final String DRAGON_JOB_CREDENTIALS_JSON =
      "dragon.job.credentials.json";

	public static final String QUEUE_NAME = "dragon.job.queuename";
	public static final String DEFAULT_QUEUE_NAME = "default";

	public static final String DRAGON_JOB_USER_CLASSPATH_FIRST = "dragon.job.user.classpath.first";

	public static final String CLASSPATH_ARCHIVES = "dragon.job.classpath.archives";

	public static final String CLASSPATH_FILES = "dragon.job.classpath.files";

	public static final String CACHE_FILES = "dragon.job.cache.files";

	public static final String CACHE_ARCHIVES = "dragon.job.cache.archives";

	public static final String CACHE_FILES_SIZES = "dragon.job.cache.files.filesizes"; // internal
																																										 // use
																																										 // only

	public static final String CACHE_ARCHIVES_SIZES = "dragon.job.cache.archives.filesizes"; // ditto

	public static final String CACHE_LOCALFILES = "dragon.job.cache.local.files";

	public static final String CACHE_LOCALARCHIVES = "dragon.job.cache.local.archives";

	public static final String CACHE_FILE_TIMESTAMPS = "dragon.job.cache.files.timestamps";

	public static final String CACHE_ARCHIVES_TIMESTAMPS = "dragon.job.cache.archives.timestamps";

	public static final String CACHE_FILE_VISIBILITIES = "dragon.job.cache.files.visibilities";

	public static final String CACHE_ARCHIVES_VISIBILITIES = "dragon.job.cache.archives.visibilities";

	public static final String CACHE_SYMLINK = "dragon.job.cache.symlink.create";

	public static final String TASK_USERLOG_LIMIT = "dragon.task.userlog.limit.kb";

	public static final String JOB_METAINFO = "job.metainfo";

	public static final String JOB_ACL_VIEW_JOB = "dragon.job.acl-view-job";

	public static final String DEFAULT_JOB_ACL_VIEW_JOB = " ";

	public static final String JOB_ACL_MODIFY_JOB = "dragon.job.acl-modify-job";

	public static final String DEFAULT_JOB_ACL_MODIFY_JOB = " ";

	public static final String JOB_CANCEL_DELEGATION_TOKEN = "dragon.job.complete.cancel.delegation.tokens";

	// This should be the directory where splits file gets localized on the node
	// running ApplicationMaster.
	public static final String JOB_SUBMIT_DIR = "graphJobSubmitDir";

	public static final String JOB_DESCRIPTION_FILE = "graphjob.desc";

	// The token file for the application. Should contain tokens for access to
	// remote file system and may optionally contain application specific tokens.
	// For now, generated by the AppManagers and used by NodeManagers and the
	// Containers.
	public static final String APPLICATION_TOKENS_FILE = "appTokens";

	public static final String APPLICATION_MASTER_CLASS = "org.apache.hadoop.realtime.server.DragonApplicationMaster";

	public static final String DRAGON_PREFIX = "yarn.app.dragon.";

	public static final String DRAGON_AM_PREFIX = DRAGON_PREFIX + "am.";

	/** Command line arguments passed to the DRAGON app master. */
	public static final String DRAGON_AM_COMMAND_OPTS = DRAGON_AM_PREFIX
	    + "command-opts";
	public static final String DEFAULT_DRAGON_AM_COMMAND_OPTS = "-Xmx1536m";

	/** The amount of memory the DRAGON app master needs. */
	public static final String DRAGON_AM_VMEM_MB = DRAGON_AM_PREFIX
	    + "resource.mb";
	public static final int DEFAULT_DRAGON_AM_VMEM_MB = 2048;

	/** Root Logging level passed to the DRAGON app master. */
	public static final String DRAGON_AM_LOG_LEVEL = DRAGON_AM_PREFIX
	    + "log.level";
	public static final String DEFAULT_DRAGON_AM_LOG_LEVEL = "INFO";

	/** The log directory for the containers */
	public static final String TASK_LOG_DIR = DRAGON_PREFIX + "container.log.dir";

	public static final String TASK_LOG_SIZE = DRAGON_PREFIX
	    + "container.log.filesize";

	/** The staging directory for dragon. */
	public static final String DRAGON_AM_STAGING_DIR = DRAGON_AM_PREFIX
	    + "staging-dir";

  public static final String APPLICATION_ATTEMPT_ID =
      "dragon.job.application.attempt.id";
  
  /** The number of client retires to the AM - before reconnecting to the RM
   * to fetch Application State. 
   */
  public static final String DRAGON_CLIENT_TO_AM_IPC_MAX_RETRIES = 
    DRAGON_PREFIX + "client-am.ipc.max-retries";
  public static final int DEFAULT_DRAGON_CLIENT_TO_AM_IPC_MAX_RETRIES = 3;
  
  /** AM ACL disabled. **/
  public static final String JOB_AM_ACCESS_DISABLED = 
    "dragon.job.am-access-disabled";
  public static final boolean DEFAULT_JOB_AM_ACCESS_DISABLED = false;
}
