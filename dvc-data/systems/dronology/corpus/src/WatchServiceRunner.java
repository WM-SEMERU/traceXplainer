package edu.nd.dronology.services.core.util;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import edu.nd.dronology.services.core.api.IFileChangeNotifyable;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class WatchServiceRunner implements Runnable {
	private static final ILogger LOGGER = LoggerProvider.getLogger(WatchServiceRunner.class);
	private boolean recursive;
	private IFileChangeNotifyable manager;
	private String[] fileExtensions;

	public WatchServiceRunner(String dir, boolean recursive, IFileChangeNotifyable manager, String... fileExtensions) {
		this.dir = dir;
		this.fileExtensions = fileExtensions;
		this.recursive = recursive;
		this.manager = manager;
	}

	private WatchService watcher;
	private String dir;
	private Object notifyTask;
	private List<String> changeList = new ArrayList<>();
	private static Map<WatchKey, Path> keys = new HashMap<>();

	@Override
	public void run() {

		try {
			watcher = FileSystems.getDefault().newWatchService();
			register(Paths.get(dir));

			while (true) {
				WatchKey watchKey = watcher.take();
				for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
					Path context = (Path) watchEvent.context();
					for (String ext : fileExtensions) {
						if (context.getFileName().toString().endsWith(ext)) {
							if ("ENTRY_DELETE".equals(watchEvent.kind().toString())
									|| "ENTRY_CREATE".equals(watchEvent.kind().toString())
									|| "ENTRY_MODIFY".equals(watchEvent.kind().toString())) {
								changed(context.getFileName().toString());
							}
						}
					}
				}
				watchKey.reset();
			}
		} catch (ClosedWatchServiceException e) {
		} catch (IOException e) {
			LOGGER.error(e);
		} catch (Exception e) {
			LOGGER.error(e);
		}

	}

	private void changed(String fileName) {
		changeList.add(fileName);
		if (notifyTask != null) {
			return;
		}
		Timer timer = new Timer();
		timer.schedule(new NotifyChangeTask(), 500);
		notifyTask = new NotifyChangeTask();
	}

	class NotifyChangeTask extends TimerTask {
		@Override
		public void run() {
			Set<String> changed = new HashSet<>();
			synchronized (changeList) {
				changed.addAll(changeList);
				changeList.clear();
				notifyTask = null;
			}
			manager.notifyFileChange(changed);

		}
	}

	private void registerAll(final Path start) throws IOException {
		// register directory and sub-directories

		Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				try {
					if (dir.getFileName() != null && (dir.getFileName().toString().startsWith(".")
							|| dir.getFileName().toString().startsWith("$"))) {
						return FileVisitResult.SKIP_SUBTREE;
					}
					register(dir);
					return FileVisitResult.CONTINUE;

				} catch (Exception e) {
					e.printStackTrace();
					return FileVisitResult.SKIP_SUBTREE;
				}
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) {
				System.err.format("Unable to register:" + " %s: %s%n", file, exc);
				return FileVisitResult.CONTINUE;
			}
		});

	}

	/**
	 * Register the given directory with the WatchService
	 */
	private void register(Path dir) throws IOException {

		// WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
		// ENTRY_MODIFY);
		WatchKey key = dir.register(watcher, ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
				StandardWatchEventKinds.ENTRY_DELETE);
		if (true) {
			// System.out.println("VALID: "+dir.getFileName().toString());
			Path prev = keys.get(key);
			if (prev == null) {
				// System.out.format("register: %s\n", dir);
			} else {
				if (!dir.equals(prev)) {
					// System.out.format("update: %s -> %s\n", prev, dir);
				}
			}
		}
		keys.put(key, dir);
	}

	public void stop() {
		try {
			watcher.close();
		} catch (IOException e) {
			LOGGER.error(e);
		}
	}

}
