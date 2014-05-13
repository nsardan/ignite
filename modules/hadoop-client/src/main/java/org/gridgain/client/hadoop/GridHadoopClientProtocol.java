/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.client.hadoop;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.ipc.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.protocol.*;
import org.apache.hadoop.mapreduce.security.token.delegation.*;
import org.apache.hadoop.mapreduce.v2.*;
import org.apache.hadoop.mapreduce.v2.jobhistory.*;
import org.apache.hadoop.mapreduce.v2.util.*;
import org.apache.hadoop.security.*;
import org.apache.hadoop.security.authorize.*;
import org.apache.hadoop.security.token.*;
import org.gridgain.client.*;
import org.gridgain.grid.hadoop.*;
import org.gridgain.grid.kernal.processors.hadoop.*;
import org.gridgain.grid.kernal.processors.hadoop.proto.*;

import java.io.*;

/**
 * Hadoop client protocol.
 */
public class GridHadoopClientProtocol implements ClientProtocol {
    /** GridGain framework name property. */
    public static final String FRAMEWORK_NAME = "gridgain";

    /** GridGain server host property. */
    public static final String PROP_SRV_ADDR = "gridgain.server.address";

    /** GridGain status poll delay property. */
    public static final String PROP_STATUS_POLL_DELAY = "gridgain.status.poll_delay";

    /** Protocol version. */
    public static final long PROTO_VER = 1L;

    /** Configuration. */
    private final Configuration conf;

    /** GG client address. */
    private final String cliAddr;

    /** GG client. */
    private volatile GridClient cli;

    /** Last received version. */
    private long lastVer = -1;

    /** Last received status. */
    private JobStatus lastStatus;

    /**
     * Constructor.
     *
     * @param conf Configuration.
     * @param cli GG client.
     */
    GridHadoopClientProtocol(Configuration conf, String cliAddr, GridClient cli) {
        assert cliAddr != null;
        assert cli != null;

        this.conf = conf;
        this.cliAddr = cliAddr;
        this.cli = cli;
    }

    /** {@inheritDoc} */
    @Override public JobID getNewJobID() throws IOException, InterruptedException {
        try {
            GridHadoopJobId jobID = cli.compute().execute(GridHadoopProtocolNextTaskIdTask.class.getName(), null);

            return new JobID(jobID.globalId().toString(), jobID.localId());
        }
        catch (GridClientException e) {
            throw new IOException("Failed to get new job ID.", e);
        }
    }

    /** {@inheritDoc} */
    @Override public JobStatus submitJob(JobID jobId, String jobSubmitDir, Credentials ts) throws IOException,
        InterruptedException {
        try {
            GridHadoopJobStatus status = cli.compute().execute(GridHadoopProtocolSubmitJobTask.class.getName(),
                new GridHadoopProtocolTaskArguments(jobId.getJtIdentifier(), jobId.getId(),
                    new GridHadoopProtocolConfigurationWrapper(conf)));

            assert status != null;

            return processStatus(status);
        }
        catch (GridClientException e) {
            throw new IOException("Failed to submit job.", e);
        }
    }

    /** {@inheritDoc} */
    @Override public ClusterMetrics getClusterMetrics() throws IOException, InterruptedException {
        // TODO

        return null;
    }

    /** {@inheritDoc} */
    @Override public Cluster.JobTrackerStatus getJobTrackerStatus() throws IOException, InterruptedException {
        return Cluster.JobTrackerStatus.RUNNING;
    }

    /** {@inheritDoc} */
    @Override public long getTaskTrackerExpiryInterval() throws IOException, InterruptedException {
        return 0;
    }

    /** {@inheritDoc} */
    @Override public AccessControlList getQueueAdmins(String queueName) throws IOException {
        return new AccessControlList("*");
    }

    /** {@inheritDoc} */
    @Override public void killJob(JobID jobid) throws IOException, InterruptedException {
        // TODO
    }

    /** {@inheritDoc} */
    @Override public void setJobPriority(JobID jobid, String priority) throws IOException, InterruptedException {
        // No-op.
    }

    /** {@inheritDoc} */
    @Override public boolean killTask(TaskAttemptID taskId, boolean shouldFail) throws IOException,
        InterruptedException {
        // TODO

        return false;
    }

    /** {@inheritDoc} */
    @Override public JobStatus getJobStatus(JobID jobId) throws IOException, InterruptedException {
        try {
            Long delay = conf.getLong(PROP_STATUS_POLL_DELAY, 0);

            GridHadoopJobStatus status = cli.compute().execute(GridHadoopProtocolJobStatusTask.class.getName(),
                new GridHadoopProtocolTaskArguments(jobId.getJtIdentifier(), jobId.getId(), delay));

            if (status == null)
                throw new IOException("Job tracker doesn't have any information about the job: " + jobId);

            return processStatus(status);
        }
        catch (GridClientException e) {
            throw new IOException("Failed to get job status: " + jobId, e);
        }
    }

    /** {@inheritDoc} */
    @Override public Counters getJobCounters(JobID jobid) throws IOException, InterruptedException {
        // TODO

        return null;
    }

    /** {@inheritDoc} */
    @Override public TaskReport[] getTaskReports(JobID jobid, TaskType type) throws IOException, InterruptedException {
        // TODO

        return new TaskReport[0];
    }

    /** {@inheritDoc} */
    @Override public String getFilesystemName() throws IOException, InterruptedException {
        return FileSystem.get(conf).getUri().toString();
    }

    /** {@inheritDoc} */
    @Override public JobStatus[] getAllJobs() throws IOException, InterruptedException {
        // TODO

        return new JobStatus[0];
    }

    /** {@inheritDoc} */
    @Override public TaskCompletionEvent[] getTaskCompletionEvents(JobID jobid, int fromEventId, int maxEvents)
        throws IOException, InterruptedException {
        // TODO

        return new TaskCompletionEvent[0];
    }

    /** {@inheritDoc} */
    @Override public String[] getTaskDiagnostics(TaskAttemptID taskId) throws IOException, InterruptedException {
        // TODO

        return new String[0];
    }

    /** {@inheritDoc} */
    @Override public TaskTrackerInfo[] getActiveTrackers() throws IOException, InterruptedException {
        // TODO

        return new TaskTrackerInfo[0];
    }

    /** {@inheritDoc} */
    @Override public TaskTrackerInfo[] getBlacklistedTrackers() throws IOException, InterruptedException {
        return new TaskTrackerInfo[0];
    }

    /** {@inheritDoc} */
    @Override public String getSystemDir() throws IOException, InterruptedException {
        Path sysDir = new Path(GridHadoop.SYS_DIR);

        return sysDir.toString();
    }

    /** {@inheritDoc} */
    @Override public String getStagingAreaDir() throws IOException, InterruptedException {
        String usr = UserGroupInformation.getCurrentUser().getShortUserName();

        Path path = MRApps.getStagingAreaDir(conf, usr);

        return path.toString();
    }

    /** {@inheritDoc} */
    @Override public String getJobHistoryDir() throws IOException, InterruptedException {
        return JobHistoryUtils.getConfiguredHistoryServerDoneDirPrefix(conf);
    }

    /** {@inheritDoc} */
    @Override public QueueInfo[] getQueues() throws IOException, InterruptedException {
        // TODO

        return new QueueInfo[0];
    }

    /** {@inheritDoc} */
    @Override public QueueInfo getQueue(String queueName) throws IOException, InterruptedException {
        // TODO

        return null;
    }

    /** {@inheritDoc} */
    @Override public QueueAclsInfo[] getQueueAclsForCurrentUser() throws IOException, InterruptedException {
        // TODO

        return new QueueAclsInfo[0];
    }

    /** {@inheritDoc} */
    @Override public QueueInfo[] getRootQueues() throws IOException, InterruptedException {
        // TODO

        return new QueueInfo[0];
    }

    /** {@inheritDoc} */
    @Override public QueueInfo[] getChildQueues(String queueName) throws IOException, InterruptedException {
        // TODO

        return new QueueInfo[0];
    }

    /** {@inheritDoc} */
    @Override public Token<DelegationTokenIdentifier> getDelegationToken(Text renewer) throws IOException,
        InterruptedException {
        // TODO

        return null;
    }

    /** {@inheritDoc} */
    @Override public long renewDelegationToken(Token<DelegationTokenIdentifier> token) throws IOException,
        InterruptedException {
        // TODO

        return 0;
    }

    /** {@inheritDoc} */
    @Override public void cancelDelegationToken(Token<DelegationTokenIdentifier> token) throws IOException,
        InterruptedException {
        // TODO
    }

    /** {@inheritDoc} */
    @Override public LogParams getLogFileParams(JobID jobID, TaskAttemptID taskAttemptID) throws IOException,
        InterruptedException {
        //TODO

        return null;
    }

    /** {@inheritDoc} */
    @Override public long getProtocolVersion(String protocol, long clientVersion) throws IOException {
        return PROTO_VER;
    }

    /** {@inheritDoc} */
    @Override public ProtocolSignature getProtocolSignature(String protocol, long clientVersion, int clientMethodsHash)
        throws IOException {
        return ProtocolSignature.getProtocolSignature(this, protocol, clientVersion, clientMethodsHash);
    }

    /**
     * Closes protocol.
     */
    void close() {
        GridHadoopClientProtocolProvider.release(cliAddr);
    }

    /**
     * Process received status update.
     *
     * @param status GG status.
     * @return Hadoop status.
     */
    private JobStatus processStatus(GridHadoopJobStatus status) {
        // IMPORTANT! This method will only work in single-threaded environment. It is valid at the moment because
        // GridHadoopClientProtocolProvider creates new instance of this class for every new job and Job class
        // serializes invocations of submitJob() and getJobStatus() methods. However, if any of these conditions will
        // change in future and either protocol will server statuses for several jobs or status update will not be
        // serialized anymore, then we have to fallback to concurrent approach (e.g. using ConcurrentHashMap).
        // (vozerov)
        if (lastVer < status.version()) {
            lastVer = status.version();

            lastStatus = GridHadoopUtils.status(status, conf);
        }
        else
            assert lastStatus != null;

        return lastStatus;
    }
}
