/*
 *	TestApplication.java
 *
 *	Copyright © 2015, TouchIn.
 *	All rights reserved.
 */
package ru.touchin.hashbot;

import android.app.Application;
import android.content.Context;
import android.os.Debug;
import android.os.Environment;
import android.os.Process;
import android.os.StrictMode;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * Application.
 *
 * @author Gleb Nikitenko
 * @version 1.0
 * @since Aug 18, 2015
 */
public final class HashBotApplication extends Application {
	
    /** The number of device cores. */
    public static int NUMBER_OF_CORES =
            Runtime.getRuntime().availableProcessors();

	/** The Debug Mode-Flag. */
	public static final boolean IS_DEBUG_MODE = false;

	/** The index of Linux Thread Priority, that will use for Main Thread. */
	private static final int MAIN_THREAD_PRIORITY_INDEX = -20;
	/** The index of Linux Thread Priority, that will use for UIL Threads. */
	private static final int IMAGE_LOADER_THREAD_PRIORITY_INDEX =
			Process.THREAD_PRIORITY_LOWEST;
	/** The image loader's size of thread-poll. */
	private static final int IMAGE_LOADER_THREAD_POOL_SIZE = NUMBER_OF_CORES;

	/** {@inheritDoc} */
	@Override
	public void onCreate() {
		super.onCreate();
		initStrictMode(IS_DEBUG_MODE);
		initImageLoader(getApplicationContext(), IS_DEBUG_MODE);
	}

	/**
	 * The image loader initialization.
	 * 
	 * @param context
	 *            Application Context
	 * @param isDebugMode
	 *            debug mode
	 */
	private static final void initImageLoader(Context context, boolean isDebugMode) {
		 final ImageLoaderConfiguration.Builder configBuilder =
	                new ImageLoaderConfiguration.Builder(context)
	                        .threadPriority(IMAGE_LOADER_THREAD_PRIORITY_INDEX)
	                        .denyCacheImageMultipleSizesInMemory()
	                        .diskCacheFileNameGenerator(new Md5FileNameGenerator())
	                        .tasksProcessingOrder(QueueProcessingType.FIFO)
	                        .memoryCacheSizePercentage(90)
	                        .threadPoolSize(IMAGE_LOADER_THREAD_POOL_SIZE);

		if (isDebugMode)
			configBuilder.writeDebugLogs();
		ImageLoader.getInstance().init(configBuilder.build());
	}

	/** Release the image loader. */
	@SuppressWarnings("unused")
	private static final void destroyImageLoader() {
		ImageLoader.getInstance().destroy();
	}

	/**
	 * The strict mode initialization.
	 * 
	 * @param isDebugMode
	 *            debugMode flag
	 **/
	private static final void initStrictMode(boolean isDebugMode) {
		if (isDebugMode) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork()
					// or
					// .detectAll()
					// for
					// all
					// detectable
					// problems
					.penaltyLog().detectCustomSlowCalls().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects()
					.detectLeakedClosableObjects()
					.penaltyLog().penaltyDeath().detectActivityLeaks().build());

			System.setErr(new PrintStreamStrictModeKills(System.err));

		} else {
			Process.setThreadPriority(Process.myTid(), MAIN_THREAD_PRIORITY_INDEX);

		}
	}

	/**
	 * Dump Memory trace
	 *
	 * @author Gleb Nikitenko
	 * @version 1.0
	 * @since Jul 29, 2015
	 */
	private static final class PrintStreamStrictModeKills extends PrintStream {

		/** {@inheritDoc} */
		public PrintStreamStrictModeKills(OutputStream out) {
			super(out);
		}

		/** {@inheritDoc} */
		public PrintStreamStrictModeKills(File file) throws FileNotFoundException {
			super(file);
		}

		/** {@inheritDoc} */
		public PrintStreamStrictModeKills(String fileName) throws FileNotFoundException {
			super(fileName);
		}

		/** {@inheritDoc} */
		public PrintStreamStrictModeKills(OutputStream out, boolean autoFlush) {
			super(out, autoFlush);
		}

		/** {@inheritDoc} */
		public PrintStreamStrictModeKills(File file, String charsetName)
				throws FileNotFoundException, UnsupportedEncodingException {
			super(file, charsetName);
		}

		/** {@inheritDoc} */
		public PrintStreamStrictModeKills(String fileName, String charsetName)
				throws FileNotFoundException, UnsupportedEncodingException {
			super(fileName, charsetName);
		}

		/** {@inheritDoc} */
		public PrintStreamStrictModeKills(OutputStream out, boolean autoFlush, String charsetName)
				throws UnsupportedEncodingException {
			super(out, autoFlush, charsetName);
		}

		/** {@inheritDoc} */
		@Override
		synchronized public final void println(String str) {
			super.println(str);
			if (str.startsWith("StrictMode VmPolicy violation with POLICY_DEATH;")) {
				// StrictMode is about to terminate us... do a heap dump!
				try {
					final File dir = Environment.getExternalStorageDirectory();
					final File file = new File(dir, "strictmode-violation.hprof");
					super.println("Dumping HPROF to: " + file);
					Debug.dumpHprofData(file.getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
