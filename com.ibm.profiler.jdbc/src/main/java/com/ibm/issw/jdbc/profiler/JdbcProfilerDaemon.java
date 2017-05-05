package com.ibm.issw.jdbc.profiler;

/*
 *-----------------------------------------------------------------
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * WebSphere Commerce
 *
 * (C) Copyright IBM Corp. 2013, 2014
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has
 * been deposited with the U.S. Copyright Office.
 *-----------------------------------------------------------------
 */

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * JdbcProfilerDaemon
 */
public final class JdbcProfilerDaemon implements JdbcEventListener {
	/**
	 * IBM Copyright notice field.
	 */
	public static final String COPYRIGHT = com.ibm.commerce.copyright.IBMCopyright.SHORT_COPYRIGHT;

	private static final boolean DAEMON_ENABLED = false;

	private static final Logger LOG = Logger.getLogger(JdbcProfilerDaemon.class
			.getName());
	private ServerSocketChannel serverSocketChannel;
	private JdbcEventQueue queue = new JdbcEventQueue();

	private static final Object QUEUE_LOCK = new Object();

	// private static final Object SEND_LOCK = new Object();

	private boolean listening = false;
	private DaemonJob daemonJob;
	// private static final int DEFAULT_PORT = 26000;
	/** port property name */
	public static final String PORT_PROPERTY = "com.ibm.issw.jdbc.profiler.port";
	private static final JdbcProfilerDaemon INSTANCE = new JdbcProfilerDaemon();

	// private static final int SO_TIMEOUT = 10000;
	// private static final int SO_LINGER = 5000;

	private JdbcProfilerDaemon() {
		if (DAEMON_ENABLED) {
			if (!this.listening) {
				try {
					int portNum = 26000;
					String portNumOveride = System
							.getProperty("com.ibm.issw.jdbc.profiler.port");
					if (portNumOveride != null) {
						try {
							portNum = Integer.valueOf(portNumOveride)
									.intValue();
						} catch (NumberFormatException e) {
							LOG.fine("Falling back to default port 26000");
						}
					}

					if (LOG.isLoggable(Level.FINE)) {
						LOG.fine("Binding to port " + portNum);
					}
					this.serverSocketChannel = ServerSocketChannel.open();
					ServerSocket socket = this.serverSocketChannel.socket();
					socket.bind(new InetSocketAddress(portNum));

					this.daemonJob = new DaemonJob();
					this.daemonJob.setName("JdbcProfilerDaemon");
					this.daemonJob.setDaemon(true);
					this.daemonJob.start();
					this.listening = true;
					if (LOG.isLoggable(Level.INFO)) {
						LOG.info("Jdbc Profiler listening to socket " + portNum);
					}
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Failed to bind socket", e);
				}
			}
		}
	}

	/**
	 * notify JDBC events
	 * @param jdbcEvents the event
	 */
	@Override
    public void notifyJdbcEvent(JdbcEvent[] jdbcEvents) {
		if (LOG.isLoggable(Level.FINE)) {
			LOG.fine("Notifying AsyncHandler");
			for (int i = 0; i < jdbcEvents.length; i++) {
				LOG.fine(jdbcEvents[i].toString());
			}

		}

		this.queue.enqueue(jdbcEvents);

		synchronized (QUEUE_LOCK) {
			QUEUE_LOCK.notify();
		}
	}

	/**
	 * 
	 * getInstance
	 * @return the instance
	 */
	public static JdbcProfilerDaemon getInstance() {
		return INSTANCE;
	}

	/**
	 * 
	 * JdbcProfilerDaemonComparator
	 */
	final static class JdbcProfilerDaemonComparator implements
			Comparator<JdbcEvent> {
		/**
		 * ctor
		 */
		JdbcProfilerDaemonComparator() {
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
        public int compare(JdbcEvent o1, JdbcEvent o2) {

			return (o1.getSequence() < o2.getSequence() ? -1 : (o1
					.getSequence() == o2.getSequence() ? 0 : 1));
		}
	}

	private class WorkerJob extends Thread {
		// private final ObjectOutputStream out;
		// private final Socket socket;

		/**
		 * 
		 * @param clientSocket
		 * @throws IOException
		 */
		public WorkerJob(Socket clientSocket) throws IOException {
			super();
			// this.socket = clientSocket;
			// this.out = new
			// ObjectOutputStream(clientSocket.getOutputStream());
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
        public void run() {
		} // Byte code:

		// 0: aload_0
		// 1: invokespecial 48 java/lang/Thread:run ()V
		// 4: invokestatic 54
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$0
		// ()Ljava/util/logging/Logger;
		// 7: ldc 56
		// 9: invokevirtual 61 java/util/logging/Logger:info
		// (Ljava/lang/String;)V
		// 12: iconst_1
		// 13: invokestatic 67
		// com/ibm/issw/jdbc/profiler/JdbcProfiler:setProfilingEnabled (Z)V
		// 16: invokestatic 54
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$0
		// ()Ljava/util/logging/Logger;
		// 19: getstatic 73 java/util/logging/Level:FINE
		// Ljava/util/logging/Level;
		// 22: invokevirtual 77 java/util/logging/Logger:isLoggable
		// (Ljava/util/logging/Level;)Z
		// 25: ifeq +11 -> 36
		// 28: invokestatic 54
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$0
		// ()Ljava/util/logging/Logger;
		// 31: ldc 79
		// 33: invokevirtual 82 java/util/logging/Logger:fine
		// (Ljava/lang/String;)V
		// 36: invokestatic 86
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$1
		// ()Ljava/lang/Object;
		// 39: dup
		// 40: astore_1
		// 41: monitorenter
		// 42: invokestatic 86
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$1
		// ()Ljava/lang/Object;
		// 45: invokevirtual 91 java/lang/Object:wait ()V
		// 48: aload_1
		// 49: monitorexit
		// 50: goto +6 -> 56
		// 53: aload_1
		// 54: monitorexit
		// 55: athrow
		// 56: aload_0
		// 57: getfield 26
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon$WorkerJob:socket
		// Ljava/net/Socket;
		// 60: invokevirtual 95 java/net/Socket:getChannel
		// ()Ljava/nio/channels/SocketChannel;
		// 63: invokevirtual 101 java/nio/channels/SocketChannel:isConnected ()Z
		// 66: ifeq +20 -> 86
		// 69: aload_0
		// 70: getfield 26
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon$WorkerJob:socket
		// Ljava/net/Socket;
		// 73: invokevirtual 102 java/net/Socket:isConnected ()Z
		// 76: ifeq +10 -> 86
		// 79: aload_0
		// 80: invokespecial 105
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon$WorkerJob:sendQueuedEvents
		// ()V
		// 83: goto +16 -> 99
		// 86: aload_0
		// 87: getfield 24
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon$WorkerJob:this$0
		// Lcom/ibm/issw/jdbc/profiler/JdbcProfilerDaemon;
		// 90: invokestatic 109
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$2
		// (Lcom/ibm/issw/jdbc/profiler/JdbcProfilerDaemon;)Lcom/ibm/issw/jdbc/profiler/JdbcEventQueue;
		// 93: invokevirtual 114 com/ibm/issw/jdbc/profiler/JdbcEventQueue:clear
		// ()V
		// 96: goto +142 -> 238
		// 99: goto -63 -> 36
		// 102: goto +136 -> 238
		// 105: astore_1
		// 106: invokestatic 54
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$0
		// ()Ljava/util/logging/Logger;
		// 109: getstatic 117 java/util/logging/Level:SEVERE
		// Ljava/util/logging/Level;
		// 112: ldc 119
		// 114: aload_1
		// 115: invokevirtual 123 java/util/logging/Logger:log
		// (Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
		// 118: goto +120 -> 238
		// 121: astore_1
		// 122: invokestatic 54
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$0
		// ()Ljava/util/logging/Logger;
		// 125: new 125 java/lang/StringBuffer
		// 128: dup
		// 129: aload_0
		// 130: invokevirtual 129
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon$WorkerJob:getName
		// ()Ljava/lang/String;
		// 133: invokestatic 135 java/lang/String:valueOf
		// (Ljava/lang/Object;)Ljava/lang/String;
		// 136: invokespecial 136 java/lang/StringBuffer:<init>
		// (Ljava/lang/String;)V
		// 139: ldc 138
		// 141: invokevirtual 142 java/lang/StringBuffer:append
		// (Ljava/lang/String;)Ljava/lang/StringBuffer;
		// 144: invokevirtual 145 java/lang/StringBuffer:toString
		// ()Ljava/lang/String;
		// 147: invokevirtual 148 java/util/logging/Logger:severe
		// (Ljava/lang/String;)V
		// 150: goto +88 -> 238
		// 153: astore_1
		// 154: invokestatic 54
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$0
		// ()Ljava/util/logging/Logger;
		// 157: getstatic 117 java/util/logging/Level:SEVERE
		// Ljava/util/logging/Level;
		// 160: ldc 150
		// 162: aload_1
		// 163: invokevirtual 123 java/util/logging/Logger:log
		// (Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
		// 166: goto +72 -> 238
		// 169: astore_1
		// 170: invokestatic 54
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$0
		// ()Ljava/util/logging/Logger;
		// 173: getstatic 117 java/util/logging/Level:SEVERE
		// Ljava/util/logging/Level;
		// 176: ldc 152
		// 178: aload_1
		// 179: invokevirtual 123 java/util/logging/Logger:log
		// (Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
		// 182: goto +56 -> 238
		// 185: astore_3
		// 186: jsr +5 -> 191
		// 189: aload_3
		// 190: athrow
		// 191: astore_2
		// 192: invokestatic 54
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon:access$0
		// ()Ljava/util/logging/Logger;
		// 195: new 125 java/lang/StringBuffer
		// 198: dup
		// 199: ldc 154
		// 201: invokespecial 136 java/lang/StringBuffer:<init>
		// (Ljava/lang/String;)V
		// 204: aload_0
		// 205: getfield 26
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon$WorkerJob:socket
		// Ljava/net/Socket;
		// 208: invokevirtual 158 java/net/Socket:getInetAddress
		// ()Ljava/net/InetAddress;
		// 211: invokevirtual 163 java/net/InetAddress:getCanonicalHostName
		// ()Ljava/lang/String;
		// 214: invokevirtual 142 java/lang/StringBuffer:append
		// (Ljava/lang/String;)Ljava/lang/StringBuffer;
		// 217: ldc 165
		// 219: invokevirtual 142 java/lang/StringBuffer:append
		// (Ljava/lang/String;)Ljava/lang/StringBuffer;
		// 222: invokevirtual 145 java/lang/StringBuffer:toString
		// ()Ljava/lang/String;
		// 225: invokevirtual 61 java/util/logging/Logger:info
		// (Ljava/lang/String;)V
		// 228: aload_0
		// 229: invokespecial 168
		// com/ibm/issw/jdbc/profiler/JdbcProfilerDaemon$WorkerJob:cleanUp ()V
		// 232: iconst_0
		// 233: invokestatic 67
		// com/ibm/issw/jdbc/profiler/JdbcProfiler:setProfilingEnabled (Z)V
		// 236: ret 2
		// 238: jsr -47 -> 191
		// 241: return
		//
		// Exception table:
		// from to target type
		// 42 53 53 finally
		// 4 105 105 java/nio/channels/AsynchronousCloseException
		// 4 105 121 java/lang/InterruptedException
		// 4 105 153 java/net/SocketException
		// 4 105 169 java/io/IOException
		// 4 182 185 finally
		// 238 241 185 finally }
		// private void cleanUp() {
		// try {
		// JdbcProfilerDaemon.this.queue.clear();
		// this.socket.close();
		// } catch (IOException e) {
		// JdbcProfilerDaemon.LOG.log(Level.SEVERE,
		// "Failed to close output stream");
		// }
		// }

		// private void sendQueuedEvents() throws IOException {
		// while (!JdbcProfilerDaemon.this.queue.isEmpty()) {
		// JdbcEvent[] events = JdbcProfilerDaemon.this.queue.dequeue();
		//
		// Arrays.sort(events, new JdbcProfilerDaemonComparator());
		//
		// synchronized (JdbcProfilerDaemon.SEND_LOCK) {
		// for (int i = 0; i < events.length; i++) {
		// if (JdbcProfilerDaemon.LOG.isLoggable(Level.FINE)) {
		// JdbcProfilerDaemon.LOG.fine("Serializing object "
		// + events[i]);
		// }
		// this.out.writeObject(events[i]);
		// events[i] = null;
		// this.out.flush();
		// this.out.reset();
		// }
		// }
		// }
		// }
	}

	private class DaemonJob extends Thread {
		private boolean running = true;
		private JdbcProfilerDaemon.WorkerJob workerJob;

		/**
		 * ctor
		 */
		public DaemonJob() {
			super();
		}


		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
        public void run() {
			super.run();

			while (this.running) {
				try {
					Socket socket = waitForConnect();

					if ((this.workerJob != null) && (this.workerJob.isAlive())) {
						this.workerJob.interrupt();
						try {
							this.workerJob.join();
						} catch (InterruptedException e) {
							JdbcProfilerDaemon.LOG.log(Level.SEVERE, getClass()
									.getName() + " was interrupted.");
						}
					}

					this.workerJob = new JdbcProfilerDaemon.WorkerJob(socket);
					this.workerJob.setName("JdbcProfilerDaemon Worker");
					this.workerJob.start();
				} catch (ClosedChannelException e) {
					JdbcProfilerDaemon.LOG.log(Level.SEVERE, "Channel closed",
							e);
				} catch (IOException e) {
					JdbcProfilerDaemon.LOG.log(Level.SEVERE,
							"Connection failed", e);
				}
			}
		}

		private Socket waitForConnect() throws IOException {
			SocketChannel channel = JdbcProfilerDaemon.this.serverSocketChannel
					.accept();
			Socket socket = channel.socket();
			socket.setSoTimeout(10000);
			socket.setSoLinger(false, 5000);
			if (JdbcProfilerDaemon.LOG.isLoggable(Level.INFO)) {
				JdbcProfilerDaemon.LOG.info("Client connected: "
						+ socket.getInetAddress().getCanonicalHostName());
			}
			return socket;
		}
	}

	/**
	 * 
	 * initializeDaemon
	 */
	public static void initializeDaemon() {
		if (DAEMON_ENABLED) {
			JdbcEventManager.addJdbcEventListener(JdbcProfilerDaemon
					.getInstance());
		}
	}
}
