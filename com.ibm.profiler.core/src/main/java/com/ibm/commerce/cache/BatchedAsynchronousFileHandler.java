package com.ibm.commerce.cache;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2014, 2015
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * The BatchedAsynchronousFileHandler is used to write java logs to file
 * asynchronously using batches. It has the same properties as the
 * java.util.FileLogger.
 */
public class BatchedAsynchronousFileHandler extends StreamHandler {

    private static final String LCK_EXT = ".lck"; //$NON-NLS-1$

    private static final int DEFAULT_COUNT = 1;

    private static final int DEFAULT_LIMIT = 0;

    private static final boolean DEFAULT_APPEND = false;

    private static final String DEFAULT_PATTERN = "%h/java%u.log"; //$NON-NLS-1$

    // maintain all file locks hold by this process
    private static final Hashtable<String, FileLock> ALL_LOCKS = new Hashtable<String, FileLock>();

    // the count of files which the output cycle through
    private int count;

    // the size limitation in byte of log file
    private int limit;

    // whether the FileHandler should open a existing file for output in append
    // mode
    private boolean append;

    // the pattern for output file name
    private String pattern;

    // maintain a LogManager instance for convenience
    private LogManager manager;

    // output stream, which can measure the output file length
    private MeasureOutputStream output;

    // used output file
    private File[] files;

    // output file lock
    FileLock lock = null;

    // current output file name
    String fileName = null;

    // current unique ID
    int uniqueID = -1;

    private String prefix = this.getClass().getName();

    /**
     * Construct a <code>FileHandler</code> using <code>LogManager</code> properties or their default value
     * 
     * @throws IOException
     *             if any IO exception happened
     * @throws SecurityException
     *             if security manager exists and it determines that caller does
     *             not have the required permissions to control this handler,
     *             required permissions include <code>LogPermission("control")</code> and other permission
     *             like <code>FilePermission("write")</code>, etc.
     */
    public BatchedAsynchronousFileHandler() throws IOException, SecurityException {
        init( false, null, null, null, null );
    }

    // init properties
    private void init( boolean needCheck, String p, Integer l, Integer c, Boolean a ) throws IOException {
        if ( needCheck ) {
            if ( null == p ) {
                throw new NullPointerException();
            }
            if ( "".equals( p ) ) { //$NON-NLS-1$
                throw new IllegalArgumentException( "Invalid pattern" ); //$NON-NLS-1$ 
            }
            if ( l < 0 || c < 1 ) {
                // logging.1B=The limit and count property must be larger than 0
                // and 1, respectively
                throw new IllegalArgumentException( "Invalid limit" ); //$NON-NLS-1$
            }
        }
        // check access
        manager = LogManager.getLogManager();

        initProperties( p, l, c, a );
        initOutputFiles();
        start();
    }

    private void initOutputFiles() throws FileNotFoundException, IOException {
        while ( true ) {
            // try to find a unique file which is not locked by other process
            uniqueID++;
            for ( int generation = 0; generation < count; generation++ ) {
                // cache all file names for rotation use
                files[generation] = new File( parseFileName( generation ) );
            }
            fileName = files[0].getAbsolutePath();
            synchronized ( ALL_LOCKS ) {
                /*
                 * if current process has held lock for this fileName continue
                 * to find next file
                 */
                if ( null != ALL_LOCKS.get( fileName ) ) {
                    continue;
                }
                if ( files[0].exists() && ( !append || files[0].length() >= limit ) ) {
                    for ( int i = count - 1; i > 0; i-- ) {
                        if ( files[i].exists() ) {
                            files[i].delete();
                        }
                        files[i - 1].renameTo( files[i] );
                    }
                }
                FileOutputStream fileStream = new FileOutputStream( fileName + LCK_EXT );
                FileChannel channel = fileStream.getChannel();
                /*
                 * if lock is unsupported and IOException thrown, just let the
                 * IOException throws out and exit otherwise it will go into an
                 * undead cycle
                 */
                lock = channel.tryLock();
                if ( null == lock ) {
                    try {
                        fileStream.close();
                    } catch ( Exception e ) // $ANALYSIS-IGNORE
                    {
                        // ignore
                    }
                    continue;
                }
                ALL_LOCKS.put( fileName, lock );
                break;
            }
        }
        output = new MeasureOutputStream( new BufferedOutputStream( new FileOutputStream( fileName, append ) ), files[0].length() );
        setOutputStream( output );
    }

    @SuppressWarnings( "nls" )
    private void initProperties( String p, Integer l, Integer c, Boolean a ) {
        initBasicProperties( "ALL", null, "java.util.logging.XMLFormatter" );
        String className = this.getClass().getName();
        pattern = null == p ? (String) getPropertyValue( className + ".pattern", DEFAULT_PATTERN ) : p;
        if ( null == pattern || "".equals( pattern ) ) {
            // logging.19=Pattern cannot be empty
            throw new NullPointerException( "Invalid pattern : null or empty" );
        }
        count = null == c ? (Integer) getPropertyValue( className + ".count", DEFAULT_COUNT ) : c.intValue();
        limit = null == l ? (Integer) getPropertyValue( className + ".limit", DEFAULT_LIMIT ) : l.intValue();
        count = count < 1 ? DEFAULT_COUNT : count;
        limit = limit < 0 ? DEFAULT_LIMIT : limit;
        files = new File[count];
        append = null == a ? (Boolean) getPropertyValue( className + ".append", DEFAULT_APPEND ) : a.booleanValue();
    }

    // print error message in some format
    private void printInvalidPropertyMessage( String key, String value, Exception e ) {
        // logging.12=Invalid property value for
        String msg = new StringBuilder().append( "Invalid property." ) //$NON-NLS-1$
            .append( prefix ).append( ":" ).append( key ).append( "/" ).append( //$NON-NLS-1$//$NON-NLS-2$
                value ).toString();

        reportError( msg, e, ErrorManager.GENERIC_FAILURE );
    }

    /**
     * init the common properties, including filter, level, formatter, and
     * encoding
     */
    void initBasicProperties( String defaultLevel, String defaultFilter, String defaultFormatter ) {
        LogManager manager = LogManager.getLogManager();

        // set filter
        String filterName = manager.getProperty( prefix + ".filter" ); //$NON-NLS-1$
        if ( null != filterName ) {
            filterName = filterName.trim();
            try {
                setFilter( (Filter) getCustomizeInstance( filterName ) );
            } catch ( Exception e1 ) {
                printInvalidPropertyMessage( "filter", filterName, e1 ); //$NON-NLS-1$
                setFilter( (Filter) getDefaultInstance( defaultFilter ) );
            }
        } else {
            setFilter( (Filter) getDefaultInstance( defaultFilter ) );
        }

        // set level
        String levelName = manager.getProperty( prefix + ".level" ); //$NON-NLS-1$
        if ( null != levelName ) {
            levelName = levelName.trim();
            try {
                setLevel( Level.parse( levelName ) );
            } catch ( Exception e ) {
                printInvalidPropertyMessage( "level", levelName, e ); //$NON-NLS-1$
                setLevel( Level.parse( defaultLevel ) );
            }
        } else {
            setLevel( Level.parse( defaultLevel ) );
        }

        // set formatter
        String formatterName = manager.getProperty( prefix + ".formatter" ); //$NON-NLS-1$
        if ( null != formatterName ) {
            formatterName = formatterName.trim();
            try {
                setFormatter( (Formatter) getCustomizeInstance( formatterName ) );
            } catch ( Exception e ) {
                printInvalidPropertyMessage( "formatter", formatterName, e ); //$NON-NLS-1$
                setFormatter( (Formatter) getDefaultInstance( defaultFormatter ) );
            }
        } else {
            setFormatter( (Formatter) getDefaultInstance( defaultFormatter ) );
        }

        // set encoding
        final String encodingName = manager.getProperty( prefix + ".encoding" ); //$NON-NLS-1$
        try {
            setEncoding( encodingName );
        } catch ( UnsupportedEncodingException e ) {
            printInvalidPropertyMessage( "encoding", encodingName, e ); //$NON-NLS-1$
        }
    }

    // get a instance from given class name, using Class.forName()
    private Object getDefaultInstance( String className ) {
        if ( className != null ) {
            try {
                return Class.forName( className ).newInstance();
            } catch ( Exception e ) // $ANALYSIS-IGNORE
            {
                // Ignored
            }
        }
        return null;
    }

    // get a instance from given class name, using context classloader
    private Object getCustomizeInstance( final String className ) throws Exception {
        Class<?> c = AccessController.doPrivileged( new PrivilegedExceptionAction<Class<?>>() {
            /*
             * (non-Javadoc)
             * 
             * @see java.security.PrivilegedExceptionAction#run()
             */
            @Override
            public Class<?> run() throws Exception {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                if ( null == loader ) {
                    loader = ClassLoader.getSystemClassLoader();
                }
                return loader.loadClass( className );
            }
        } );
        return c.newInstance();
    }

    private void findNextGeneration() {
        super.close();
        for ( int i = count - 1; i > 0; i-- ) {
            if ( files[i].exists() ) {
                files[i].delete();
            }
            files[i - 1].renameTo( files[i] );
        }
        try {
            // $ANALYSIS-IGNORE
            output = new MeasureOutputStream( new BufferedOutputStream( new FileOutputStream( files[0] ) ) );
        } catch ( FileNotFoundException e1 ) {
            // logging.1A=Error happened when open log file.
            this.getErrorManager().error( "BatchedAsynchronousFileHandler Output stream open failure. ", //$NON-NLS-1$
                e1, ErrorManager.OPEN_FAILURE );
        }
        setOutputStream( output );
    }

    /**
     * Transform the pattern to the valid file name, replacing any patterns, and
     * applying generation and uniqueID if present
     * 
     * @param gen
     *            generation of this file
     * @return transformed filename ready for use
     */
    private String parseFileName( int gen ) {
        int cur = 0;
        int next = 0;
        boolean hasUniqueID = false;
        boolean hasGeneration = false;

        String homePath = System.getProperty( "user.home" ); //$NON-NLS-1$
        if ( homePath == null ) {
            throw new NullPointerException();
        }
        boolean homePathHasSepEnd = homePath.endsWith( File.separator );

        String tempPath = System.getProperty( "java.io.tmpdir" ); //$NON-NLS-1$
        tempPath = tempPath == null ? homePath : tempPath;
        boolean tempPathHasSepEnd = tempPath.endsWith( File.separator );

        StringBuilder sb = new StringBuilder();
        pattern = pattern.replace( '/', File.separatorChar );

        char[] value = pattern.toCharArray();
        while ( ( next = pattern.indexOf( '%', cur ) ) >= 0 ) {
            if ( ++next < pattern.length() ) {
                switch ( value[next] ) {
                    case 'g':
                        sb.append( value, cur, next - cur - 1 ).append( gen );
                        hasGeneration = true;
                        break;
                    case 'u':
                        sb.append( value, cur, next - cur - 1 ).append( uniqueID );
                        hasUniqueID = true;
                        break;
                    case 't':
                        /*
                         * we should probably try to do something cute here like
                         * lookahead for adjacent '/'
                         */
                        sb.append( value, cur, next - cur - 1 ).append( tempPath );
                        if ( !tempPathHasSepEnd ) {
                            sb.append( File.separator );
                        }
                        break;
                    case 'h':
                        sb.append( value, cur, next - cur - 1 ).append( homePath );
                        if ( !homePathHasSepEnd ) {
                            sb.append( File.separator );
                        }
                        break;
                    case '%':
                        sb.append( value, cur, next - cur - 1 ).append( '%' );
                        break;
                    default:
                        sb.append( value, cur, next - cur );
                }
                cur = ++next;
            } else {
                // fail silently
            }
        }

        sb.append( value, cur, value.length - cur );

        if ( !hasGeneration && count > 1 ) {
            sb.append( "." ).append( gen ); //$NON-NLS-1$
        }

        if ( !hasUniqueID && uniqueID > 0 ) {
            sb.append( "." ).append( uniqueID ); //$NON-NLS-1$
        }

        return sb.toString();
    }

    private Object getPropertyValue( String key, Object defaultValue ) {
        String propertyValue = manager.getProperty( key );
        if ( propertyValue != null ) {
            try {
                if ( defaultValue instanceof Boolean ) {
                    return Boolean.parseBoolean( propertyValue );
                } else if ( defaultValue instanceof Integer ) {
                    return Integer.parseInt( propertyValue );
                } else if ( defaultValue instanceof String ) {
                    return propertyValue;
                }
            } catch ( Exception e ) // $ANALYSIS-IGNORE
            {
                // Ignored
            }
        }
        return defaultValue;
    }

    /**
     * Construct a <code>BatchedAsynchronousFileHandler</code>, the given name
     * pattern is used as output filename, the file limit is set to zero(no
     * limit), and the file count is set to one, other configuration using <code>LogManager</code> properties or their
     * default value
     * 
     * This handler write to only one file and no amount limit.
     * 
     * @param pattern
     *            the name pattern of output file
     * @throws IOException
     *             if any IO exception happened
     * @throws SecurityException
     *             if security manager exists and it determines that caller does
     *             not have the required permissions to control this handler,
     *             required permissions include <code>LogPermission("control")</code> and other permission
     *             like <code>FilePermission("write")</code>, etc.
     * @throws NullPointerException
     *             if the pattern is <code>null</code>.
     * @throws IllegalArgumentException
     *             if the pattern is empty.
     */
    public BatchedAsynchronousFileHandler( String pattern ) throws IOException, SecurityException, NullPointerException, IllegalArgumentException {
        init( true, pattern, DEFAULT_LIMIT, DEFAULT_COUNT, null );
    }

    /**
     * Construct a <code>BatchedAsynchronousFileHandler</code>, the given name
     * pattern is used as output filename, the file limit is set to zero(i.e. no
     * limit applies), the file count is initialized to one, and the value of <code>append</code> becomes the new
     * instance's append mode. Other
     * configuration is done using <code>LogManager</code> properties.
     * 
     * This handler write to only one file and no amount limit.
     * 
     * @param pattern
     *            the name pattern of output file
     * @param append
     *            the append mode
     * @throws IOException
     *             if any IO exception happened
     * @throws SecurityException
     *             if security manager exists and it determines that caller does
     *             not have the required permissions to control this handler,
     *             required permissions include <code>LogPermission("control")</code> and other permission
     *             like <code>FilePermission("write")</code>, etc.
     * @throws NullPointerException
     *             if the pattern is <code>null</code>.
     * @throws IllegalArgumentException
     *             if the pattern is empty.
     */
    public BatchedAsynchronousFileHandler( String pattern, boolean append ) throws IOException, SecurityException, NullPointerException, IllegalArgumentException {
        init( true, pattern, DEFAULT_LIMIT, DEFAULT_COUNT, append );
    }

    /**
     * Construct a <code>BatchedAsynchronousFileHandler</code>, the given name
     * pattern is used as output filename, the file limit is set to given limit
     * argument, and the file count is set to given count argument, other
     * configuration using <code>LogManager</code> properties or their default
     * value
     * 
     * This handler is configured to write to a rotating set of count files,
     * when the limit of bytes has been written to one output file, another file
     * will be opened instead.
     * 
     * @param pattern
     *            the name pattern of output file
     * @param limit
     *            the data amount limit in bytes of one output file, cannot less
     *            than one
     * @param count
     *            the maximum number of files can be used, cannot less than one
     * @throws IOException
     *             if any IO exception happened
     * @throws SecurityException
     *             if security manager exists and it determines that caller does
     *             not have the required permissions to control this handler,
     *             required permissions include <code>LogPermission("control")</code> and other permission
     *             like <code>FilePermission("write")</code>, etc.
     * @throws NullPointerException
     *             if pattern is <code>null</code>.
     * @throws IllegalArgumentException
     *             if count<1, or limit<0
     */
    public BatchedAsynchronousFileHandler( String pattern, int limit, int count ) throws IOException, SecurityException, NullPointerException, IllegalArgumentException {
        init( true, pattern, limit, count, null );
    }

    /**
     * Construct a <code>BatchedAsynchronousFileHandler</code>, the given name
     * pattern is used as output filename, the file limit is set to given limit
     * argument, the file count is set to given count argument, and the append
     * mode is set to given append argument, other configuration using <code>LogManager</code> properties or their
     * default value
     * 
     * This handler is configured to write to a rotating set of count files,
     * when the limit of bytes has been written to one output file, another file
     * will be opened instead.
     * 
     * @param pattern
     *            the name pattern of output file
     * @param limit
     *            the data amount limit in bytes of one output file, cannot less
     *            than one
     * @param count
     *            the maximum number of files can be used, cannot less than one
     * @param append
     *            the append mode
     * @throws IOException
     *             if any IO exception happened
     * @throws SecurityException
     *             if security manager exists and it determines that caller does
     *             not have the required permissions to control this handler,
     *             required permissions include <code>LogPermission("control")</code> and other permission
     *             like <code>FilePermission("write")</code>, etc.
     * @throws NullPointerException
     *             if pattern is <code>null</code>.
     * @throws IllegalArgumentException
     *             if count<1, or limit<0
     */
    public BatchedAsynchronousFileHandler( String pattern, int limit, int count, boolean append ) throws IOException, SecurityException, NullPointerException, IllegalArgumentException {
        init( true, pattern, limit, count, append );
    }

    /**
     * There is no way, at the library/vm level, to know when the fileHanlder
     * will be available for closing. If the user doesn't close it in his code,
     * the finalize() will run (eventually ?) and close all opened files.
     * 
     * @throws Throwable
     *             if anything goes wrong.
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    /**
     * This output stream use decorator pattern to add measure feature to
     * OutputStream which can detect the total size(in bytes) of output, the
     * initial size can be set
     */
    static class MeasureOutputStream extends OutputStream {

        OutputStream wrapped;

        long length;

        public MeasureOutputStream( OutputStream stream, long currentLength ) {
            wrapped = stream;
            length = currentLength;
        }

        public MeasureOutputStream( OutputStream stream ) {
            this( stream, 0 );
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.OutputStream#write(int)
         */
        @Override
        public void write( int oneByte ) throws IOException {
            wrapped.write( oneByte );
            length++;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.OutputStream#write(byte[])
         */
        @Override
        public void write( byte[] bytes ) throws IOException {
            wrapped.write( bytes );
            length += bytes.length;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        @Override
        public void write( byte[] b, int off, int len ) throws IOException {
            wrapped.write( b, off, len );
            length += len;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.OutputStream#close()
         */
        @Override
        public void close() throws IOException {
            wrapped.close();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.OutputStream#flush()
         */
        @Override
        public void flush() throws IOException {
            wrapped.flush();
        }

        /**
         * 
         * getLength
         * 
         * @return length
         */
        public long getLength() {
            return length;
        }

        /**
         * 
         * setLength
         * 
         * @param newLength
         *            new length
         */
        public void setLength( long newLength ) {
            length = newLength;
        }
    }

    /**
     * IBM Copyright notice field.
     */
    public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

    /**
     * Initial maximum number of pending operation records to keep in memory
     * before writing.
     */
    private static final int INITIAL_MAXIMUM_PENDING_SIZE = 100000;

    /**
     * Initial number of pending operation records before triggering a write
     * operation.
     */
    private static final int INITIAL_PENDING_FLUSH_SIZE = INITIAL_MAXIMUM_PENDING_SIZE / 2;

    /**
     * Initial maximum amount of time to wait for the writer thread to stop in
     * milliseconds.
     */
    private static final long INITIAL_THREAD_JOIN_TIMEOUT = 60000;

    /**
     * Initial maximum amount of time for the writing thread to sleep before
     * verifying if it needs to write a new batch of items.
     */
    private static final long INITIAL_WRITER_MAXIMUM_SLEEP_TIME = 100;

    /**
     * List of pending records. Its size will never go above the value
     * configured in {@link #maximumPendingSize}.
     */
    private ConcurrentLinkedQueue<LogRecord> pendingRecords = new ConcurrentLinkedQueue<LogRecord>();

    /**
     * Maximum size that the {@link #pendingRecords} list will be allowed to
     * reach. Ensures that the amount of memory consumed by records gathering
     * will never go above this defined limit.
     */
    private int maximumPendingSize = INITIAL_MAXIMUM_PENDING_SIZE;

    /**
     * Minimum number of records in the pending {@link #pendingRecords} list to
     * reach before waiting the writing thread to write the pending data.
     */
    private int pendingFlushSize = INITIAL_PENDING_FLUSH_SIZE;

    /**
     * Maximum amount of time the writing thread is allowed to sleep before
     * writing any pending records.
     */
    private long writerMaximumSleepTime = INITIAL_WRITER_MAXIMUM_SLEEP_TIME;

    /**
     * Maximum amount of time to wait for the writing thread to finish it's work
     * when stopping the record gatherer.
     */
    private long threadJoinTimeout = INITIAL_THREAD_JOIN_TIMEOUT;

    /**
     * record writing thread runnable object.
     */
    private RecordWriterRunnable recordWriter = new RecordWriterRunnable();

    /**
     * The record gathering thread running {@link #recordWriter}.
     */
    private Thread recordWriterThread = new Thread( recordWriter, "BatchedAsynchronousFileHandler log writer." );

    /**
     * Flag indicating of the record gatherer is running or not. It is consumed
     * by the {@link #recordWriter} runnable to detect when it should exit.
     */
    private boolean running = false;

    /**
     * Flag indicating that exceptions were caught in the record writing thread.
     * While this flag is set to true, all exceptions will be logged with the {@link Level#FINE} level instead of
     * {@link Level#WARNING} level.
     */
    private boolean isRunningWithExceptions = false;

    /**
     * object used to make threads wait while the buffer of performance logs to
     * write is too full.
     */
    private Object bufferFullWait = new Object();

    /**
     * queue size
     */
    private AtomicInteger bufferSize = new AtomicInteger( 0 );

    /*
     * (non-Javadoc)
     * 
     * @see java.util.logging.StreamHandler#publish(java.util.logging.LogRecord)
     */
    @Override
    public void publish( LogRecord record ) {
        int size = bufferSize.get();

        boolean running = isRunning();

        if ( running ) {
            pendingRecords.add( record );
            bufferSize.incrementAndGet();
            size += 1;
        }

        if ( size >= pendingFlushSize ) {
            wakeWriteThread();
        }

        if ( size >= maximumPendingSize ) {
            waitForWrite();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.logging.StreamHandler#close()
     */
    @Override
    public void close() {
        if ( isRunning() ) {
            setRunning( false );
            joinQuietly( recordWriterThread, threadJoinTimeout );
        }

        // release locks
        super.close();
        ALL_LOCKS.remove( fileName );
        try {
            FileChannel channel = lock.channel();
            lock.release();
            channel.close();
            new File( fileName + LCK_EXT ).delete();
        } catch ( IOException e ) // $ANALYSIS-IGNORE
        {
            // ignore
        }
    }

    /**
     * Join a thread quietly for a maximum amount of time.
     * 
     * @param threadToJoin
     *            the thread to join with
     * @param threadJoinTimeout
     *            the maximum amount of time to wait in milliseconds.
     * @return true if the join operation was successfully completed in time.
     */
    public static boolean joinQuietly( Thread threadToJoin, long threadJoinTimeout ) {
        if ( threadToJoin != null ) {
            try {
                threadToJoin.join( threadJoinTimeout );
                return true;
            } catch ( InterruptedException e ) // $ANALYSIS-IGNORE
            {

            }
        }
        return false;
    }

    /**
     * record writing runnable definition
     */
    private class RecordWriterRunnable implements Runnable {

        /**
         * record writing thread run entry method.
         */
        @Override
        public void run() {
            runRecordWriter();
        }

    }

    /**
     * run the record writing thread
     */
    private void runRecordWriter() {
        while ( true ) {
            waitSilently( recordWriter, writerMaximumSleepTime );

            boolean keepRunning = true;
            while ( keepRunning ) {
                keepRunning = writeRecords();
            }

            if ( !isRunning() ) {

                writeRecords();

                executeBeforeBackgroundThreadStop();

                break;
            }
        }
    }

    /**
     * wait on a specified object silently
     * 
     * @param objectToWait
     *            the object to wait on
     * @param waitTime
     *            the maximum amount of time to wait in milliseconds.
     * 
     * @return true if the wait operation was successful without interruptions.
     */
    public static boolean waitSilently( Object objectToWait, long waitTime ) {
        if ( objectToWait != null ) {
            try {
                synchronized ( objectToWait ) {
                    objectToWait.wait( waitTime );
                }
                return true;
            } catch ( InterruptedException e ) // $ANALYSIS-IGNORE
            {
                // ignore the exception, we expect to be interrupted
            }
        }
        return false;
    }

    /**
     * Optional method that sub classes may override in order to inject code
     * before stopping their record writing thread. Usually used to close off
     * resources such as network sockets, database connections or files.
     */
    protected void executeBeforeBackgroundThreadStop() {

    }

    /**
     * this method is synchronized since it
     * 
     * @return true if the application is running
     */
    public synchronized boolean isRunning() {
        return running;
    }

    /**
     * @param running
     *            set the new running state
     */
    protected synchronized void setRunning( boolean running ) {
        this.running = running;
    }

    /**
     * Start the record gathering writing thread.
     */
    public void start() {
        if ( isRunning() ) {
            return;
        }
        recordWriterThread.start();
        setRunning( true );
    }

    private void waitForWrite() {
        synchronized ( bufferFullWait ) {
            waitSilently( bufferFullWait, 1000 );
        }
    }

    private void notifyAllOfWrite() {
        synchronized ( bufferFullWait ) {
            bufferFullWait.notifyAll();
        }
    }

    /**
     * @return the pending flush size
     */
    public int getPendingFlushSize() {
        return pendingFlushSize;
    }

    /**
     * @param pendingFlushSize
     *            the new pending flush size
     */
    public void setPendingFlushSize( int pendingFlushSize ) {
        this.pendingFlushSize = pendingFlushSize;
    }

    /**
     * Attempt to wake the record gathering thread.
     */
    private void wakeWriteThread() {
        synchronized ( recordWriter ) {
            recordWriter.notify();
        }
    }

    /**
     * This method is automatically called by the record gathering thread in
     * order to flush the pending records. It's implementation depends on the
     * concrete class.
     * 
     * @return true if a new record was written.
     */
    public boolean writeRecords() {
        boolean retVal = false;
        try {

            while ( true ) {
                LogRecord record = pendingRecords.poll();
                if ( record == null ) {
                    break;
                } else {
                    bufferSize.decrementAndGet();
                    super.publish( record );

                    if ( limit > 0 && output.getLength() >= limit ) {
                        flush();
                        AccessController.doPrivileged( new PrivilegedAction<Object>() {
                            /*
                             * (non-Javadoc)
                             * 
                             * @see java.security.PrivilegedAction#run()
                             */
                            @Override
                            public Object run() {
                                findNextGeneration();
                                return null;
                            }
                        } );
                    }

                    retVal = true;
                }
            }

            setRunningWithExceptions( false );

        } catch ( Exception ex ) {
            logWriteRecordException( ex );
        }

        flush();
        notifyAllOfWrite();

        return retVal;
    }

    /**
     * @return true if the writer is running with exceptions
     */
    protected boolean isRunningWithExceptions() {
        return isRunningWithExceptions;
    }

    /**
     * @param isRunningWithExceptions
     *            set to true if the writer is running with exceptions
     */
    public void setRunningWithExceptions( boolean isRunningWithExceptions ) {
        this.isRunningWithExceptions = isRunningWithExceptions;
    }

    /**
     * Utility method to fetch the log level of potentially recurring exceptions
     * during the record writing process.
     * 
     * @return the log level to use while logging exceptions during the write
     *         process.
     */
    protected Level getWriteRecordErrorLogLevel() {
        boolean runningWithExceptions = isRunningWithExceptions();
        Level logLevel = Level.WARNING;
        if ( runningWithExceptions ) {
            logLevel = Level.FINE;
        }
        return logLevel;
    }

    /**
     * Utility method to log exceptions during the write process. This method
     * will use the appropriate log level specified in {@link #getWriteRecordErrorLogLevel()} and ensure that future
     * exceptions
     * use the correct log level.
     * 
     * @param ex
     *            the exception to log.
     */
    protected void logWriteRecordException( Exception ex ) {
        // check to avoid logging exceptions multiple times.
        reportError( "failed to write log record", ex, ErrorManager.GENERIC_FAILURE );
    }

    /**
     * @return the maximum pending record list size
     */
    public int getMaximumPendingSize() {
        return maximumPendingSize;
    }

    /**
     * @param maximumPendingSize
     *            the maximum pending record list size
     */
    public void setMaximumPendingSize( int maximumPendingSize ) {
        this.maximumPendingSize = maximumPendingSize;
    }

    /**
     * @return the maximum time for the writer thread to sleep
     */
    public long getWriterMaximumSleepTime() {
        return writerMaximumSleepTime;
    }

    /**
     * @param writerMaximumSleepTime
     *            the maximum time for the writer thread to sleep
     */
    public void setWriterMaximumSleepTime( long writerMaximumSleepTime ) {
        this.writerMaximumSleepTime = writerMaximumSleepTime;
    }

    /**
     * @return the maximum time to wait for the writer thread to complete its
     *         work
     */
    public long getThreadJoinTimeout() {
        return threadJoinTimeout;
    }

    /**
     * @param threadJoinTimeout
     *            the maximum time to wait for the writer thread to complete its
     *            work
     */
    public void setThreadJoinTimeout( long threadJoinTimeout ) {
        this.threadJoinTimeout = threadJoinTimeout;
    }

}
