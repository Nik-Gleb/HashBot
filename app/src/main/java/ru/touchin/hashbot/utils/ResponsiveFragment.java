/*
 *	ResponsiveFragment.java
 *
 *	Copyright &copy; 2015, TouchIn.
 *	All rights reserved.
 */
package ru.touchin.hashbot.utils;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import java.io.Closeable;
import java.lang.ref.WeakReference;
import java.util.Vector;

/**
 * Responsive Fragment
 *
 * @author Gleb Nikitenko
 * @version 1.0
 * @since Jul 29, 2015
 */
public abstract class ResponsiveFragment extends Fragment {

	/** The Thread Name format. */
	private static final String THREAD_NAME_FORMAT = "%s - looper";
	/** The Background Thread priority. */
	private static final int BACKGROUND_THREAD_PRIORITY = Process.THREAD_PRIORITY_LOWEST;

	/** Need Save State when rotate. */
	private static final boolean needRetainInstance = true;
	/**
	 * Need Long Life Cycle (onCreate-onDestroy), otherwise (onStart-onStop).
	 */
	private static final boolean needLongLifeCycle = true;
	/** Need Pause-Resume Life Cycle, otherwise (onStart-onStop). */
	private static final boolean needPauseResumeLifeCycle = false;

	/** The Handlers Connector. */
	private HandlersConnector mHandlersConnector = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			if (needRetainInstance)
				setRetainInstance(true);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		if (needLongLifeCycle)
			create();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onDestroy()
	 */
	@Override
	public final void onDestroy() {
		if (needLongLifeCycle)
			destroy();
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onStart()
	 */
	@Override
	public final void onStart() {
		super.onStart();
		if (!needPauseResumeLifeCycle)
			onBegin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onStop()
	 */
	@Override
	public final void onStop() {
		if (!needPauseResumeLifeCycle)
			onEnd();
		super.onStop();
	}

	/** {@inheritDoc} */
	@Override
	public void onResume() {
		super.onResume();
		if (needPauseResumeLifeCycle)
			onBegin();
	}

	/** {@inheritDoc} */
	@Override
	public void onPause() {
		if (needPauseResumeLifeCycle)
			onEnd();
		super.onPause();
	}

	/** Abstract start. */
	private final void onBegin() {
		if (!needLongLifeCycle)
			create();
		start();
	}

	/** Abstract stop. */
	private final void onEnd() {
		stop();
		if (!needLongLifeCycle)
			destroy();
	}

	/** Create Hub. */
	private void create() {
		mHandlersConnector = new HandlersConnector(String.format(THREAD_NAME_FORMAT,
				getClass().getSimpleName()), BACKGROUND_THREAD_PRIORITY, this);
		mHandlersConnector.start();
	}

	/** Destroy Hub. */
	private void destroy() {
		mHandlersConnector.quit();
		mHandlersConnector = null;
	}

	/** Start Hub. */
	private final void start() {
		mHandlersConnector.onResume();
	}

	/** Stop Hub. */
	private final void stop() {
		mHandlersConnector.onPause();
	}

	/**
	 * @return ForeGround Handler
	 */
	protected final Handler getFgHandler() {
		return mHandlersConnector != null ? mHandlersConnector.getFgHandler() : null;
	}

	/**
	 * @return BackGroundHandler
	 */
	protected final Handler getBgHandler() {
		return mHandlersConnector != null ? mHandlersConnector.getBgHandler() : null;
	}

	/** Foreground Initialization. */
	protected abstract void fg_init();

	/**
	 * @param msg
	 *            to Foreground Handling.
	 */
	protected abstract void fg_handle(Message msg);

	/** Foreground recycling. */
	protected abstract void fg_destroy();

	/** Background Initialization. */
	protected abstract void bg_init();

	/**
	 * @param msg
	 *            to Background Handling.
	 */
	protected abstract void bg_handle(Message msg);

	/** Background recycling. */
	protected abstract void bg_destroy();

	/**
	 * Handlers Connector.
	 *
	 * @author Gleb Nikitenko
	 * @version 1.0
	 * @since Jul 29, 2015
	 */
	private final class HandlersConnector extends HandlerThread implements Callback {

		/** The ForegroundGround Handler. */
		private final ForegroundHandler mForegroundHandler;
		/** The BackGround Handler. */
		private Handler mBackgroundHandler = null;

		/** The Weak Fragment. */
		private final WeakReference<ResponsiveFragment> mWeakFragment;

		/**
		 * Constructs a new BackgroundHandlerThread with name & fragment.
		 *
		 * @param name
		 *            The name of this Thread
		 * @param fragment
		 *            responsive fragment
		 */
		public HandlersConnector(String name, ResponsiveFragment fragment) {
			super(name);
			mWeakFragment = new WeakReference<ResponsiveFragment>(fragment);
			mForegroundHandler = new ForegroundHandler();
		}

		/**
		 * Constructs a new BackgroundHandlerThread with name, priority,
		 * callbacks.
		 *
		 * @param name
		 *            The name of this Thread
		 * @param priority
		 *            The THREAD-Priority for this handler
		 * @param fragment
		 *            responsive fragment
		 */
		public HandlersConnector(String name, int priority, ResponsiveFragment fragment) {
			super(name, priority);
			mWeakFragment = new WeakReference<ResponsiveFragment>(fragment);
			mForegroundHandler = new ForegroundHandler();
		}

		/** {@inheritDoc} */
		@Override
		protected final void onLooperPrepared() {
			super.onLooperPrepared();
			mBackgroundHandler = new Handler(getLooper(), this);
			final ResponsiveFragment fragment = mWeakFragment.get();
			if (fragment != null) {
				mForegroundHandler.post(new InitRunnable(fragment));
				fragment.bg_init();
			}
		}

		/** {@inheritDoc} */
		@Override
		public final boolean quit() {
			final ResponsiveFragment fragment = mWeakFragment.get();
			if (mBackgroundHandler != null && fragment != null) {
				mWeakFragment.clear();
				mForegroundHandler.close();
				fragment.fg_destroy();
				return mBackgroundHandler.post(new QuitRunnable(mBackgroundHandler, fragment));
			} else
				return super.quit();
		}

		/**
		 * Handle in background
		 * 
		 * @param msg
		 *            message
		 * @return result
		 */
		@Override
		public final boolean handleMessage(Message msg) {
			final ResponsiveFragment fragment = mWeakFragment.get();
			if (fragment != null) {
				fragment.bg_handle(msg);
				return true;
			} else
				return false;
		}

		/** OnPause Fragment */
		final void onPause() {
			mForegroundHandler.onPause();
		}

		/** OnResume Fragment */
		final void onResume() {
			mForegroundHandler.onResume(mWeakFragment.get());
		}

		/**
		 * @return Foreground Handler.
		 */
		final Handler getFgHandler() {
			return mForegroundHandler;
		}

		/**
		 * @return Background Handler.
		 */
		final Handler getBgHandler() {
			return mBackgroundHandler;
		}

		/**
		 * Quit Runnable.
		 *
		 * @author Gleb Nikitenko
		 * @version 1.0
		 * @since Jul 29, 2015
		 */
		private final class QuitRunnable implements Runnable {

			/** The Weak Handler. */
			private final WeakReference<Handler> mWeakHandler;
			/** The Weak Fragment. */
			private final WeakReference<ResponsiveFragment> mWeakFragment;

			/**
			 * Constructs a new QuitRunnable with handler & callbacks.
			 * 
			 * @param handler
			 *            background handler
			 */
			public QuitRunnable(Handler handler, ResponsiveFragment fragment) {
				mWeakHandler = new WeakReference<Handler>(handler);
				mWeakFragment = new WeakReference<ResponsiveFragment>(fragment);
			}

			/** {@inheritDoc} */
			@Override
			public final void run() {
				final ResponsiveFragment fragment = mWeakFragment.get();
				if (fragment != null) {
					fragment.bg_destroy();
					mWeakFragment.clear();
				}
				final Handler handler = mWeakHandler.get();
				if (handler != null) {
					handler.removeCallbacksAndMessages(null);
					final Looper looper = handler.getLooper();
					if (looper != null)
						looper.quit();
					mWeakHandler.clear();
				}
			}

		}

		/**
		 * Init Runnable.
		 *
		 * @author Gleb Nikitenko
		 * @version 1.0
		 * @since Jul 29, 2015
		 */
		private final class InitRunnable implements Runnable {

			/** The Weak Fragment. */
			private final WeakReference<ResponsiveFragment> mWeakFragment;

			/**
			 * Constructs a new InitRunnable with fragment.
			 * 
			 * @param fragment
			 *            fragment
			 */
			public InitRunnable(ResponsiveFragment fragment) {
				mWeakFragment = new WeakReference<ResponsiveFragment>(fragment);
			}

			/** {@inheritDoc} */
			@Override
			public final void run() {
				final ResponsiveFragment fragment = mWeakFragment.get();
				if (fragment == null)
					return;
				fragment.fg_init();
				mWeakFragment.clear();
			}

		}

		/**
		 * Background Handler for Responsive Fragment.
		 *
		 * @author Gleb Nikitenko
		 * @version 1.0
		 * @since Jul 29, 2015
		 */
		@SuppressLint("HandlerLeak")
		private final class ForegroundHandler extends Handler implements Closeable {

			/** Message Queue Buffer. */
			private final Vector<Message> messageQueueBuffer = new Vector<Message>();
			/** The Callback Weak Reference. */
			private WeakReference<ResponsiveFragment> weakFragment = null;
			/** The terminated flag. */
			private boolean isTerminated = true;

			/** Constructs a new BackgroundHandler. */
			public ForegroundHandler() {
				isTerminated = false;
			}

			/** {@inheritDoc} */
			@Override
			public final void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (isTerminated)
					return;

				if (weakFragment != null) {
					final ResponsiveFragment fragment = weakFragment.get();
					if (fragment == null) {
						this.close();
						return;
					} else
						fragment.fg_handle(msg);
				} else
					storeMessage(msg);
			}

			/**
			 * Store Message if need
			 * 
			 * @param msg
			 *            message
			 * @return true if it was storing
			 */
			private boolean storeMessage(Message msg) {
				if (msg == null || msg.arg1 != 0)
					return false;
				else {
					final Message msgCopy = new Message();
					msgCopy.copyFrom(msg);
					messageQueueBuffer.add(msgCopy);
					return true;
				}
			}

			/** On fragment resume. */
			public final void onResume(ResponsiveFragment fragment) {
				if (!isTerminated) {
					if (fragment == null)
						throw new IllegalArgumentException("Fragment can't be null!");

					weakFragment = new WeakReference<ResponsiveFragment>(fragment);

					while (messageQueueBuffer.size() > 0) {
						final Message msg = messageQueueBuffer.elementAt(0);
						messageQueueBuffer.removeElementAt(0);
						this.handleMessage(msg);
					}
				}
			}

			/** On fragment pause. */
			public final void onPause() {
				if (weakFragment != null && !isTerminated) {
					weakFragment.clear();
					weakFragment = null;
				}
			}

			/** {@inheritDoc} */
			@Override
			public void close() {
				if (!isTerminated) {
					onPause();
					messageQueueBuffer.clear();
					isTerminated = true;
				}
			}

		}

	}

}
