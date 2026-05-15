package edu.nd.dronology.services.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.nd.dronology.services.core.api.IFileChangeNotifyable;
import edu.nd.dronology.util.FileUtil;
import edu.nd.dronology.util.NamedThreadFactory;
import net.mv.logging.ILogger;
import net.mv.logging.LoggerProvider;

public class FileManager {

	private static final ILogger LOGGER = LoggerProvider.getLogger(FileManager.class);
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(20,
			new NamedThreadFactory("Directory-WatchServices"));

	private WatchServiceRunner directoryWatcher;
	private String extension;
	private String path;

	public FileManager(final IFileChangeNotifyable instance, String path, String extension) {
		this.path = path;
		this.extension = extension;
		initWorkspace();

		directoryWatcher = new WatchServiceRunner(path, false, instance, extension);
		EXECUTOR_SERVICE.submit(directoryWatcher);

	}

	private void initWorkspace() {
		File f = new File(path);
		if (!f.exists()) {
			f.mkdirs();
		}

	}

	public byte[] getFileAsByteArray(String fileId) throws DronologyServiceException {
		LOGGER.info("File '" + fileId + "' requested");

		String savelocation = path + File.separator + fileId + "." + extension;
		File f = new File(savelocation);
		if (!f.exists()) {
			LOGGER.info("File not found: " + savelocation);
			throw new DronologyServiceException("File " + fileId + " does not exist on server!");
		}
		byte[] content = FileUtil.toByteArray(f);
		if (content.length == 0) {
			throw new DronologyServiceException("Error when reading file " + fileId);
		}
		return content;

	}

	public File getFile(String fileId) throws DronologyServiceException {
		LOGGER.info("File '" + fileId + "' requested");

		String savelocation = path + File.separator + fileId + "." + extension;
		File f = new File(savelocation);
		if (!f.exists()) {
			LOGGER.info("File not found: " + savelocation);
			throw new DronologyServiceException("File " + fileId + " does not exist on server!");
		}
		return f;
	}

	public boolean saveByteArrayToFile(String id, byte[] content) {
		LOGGER.info("File '" + id + "' received");
		FileOutputStream stream = null;
		String savelocation = path + File.separator + id + "." + extension;
		File f = new File(savelocation);
		if (f.exists()) {
			LOGGER.info("Deleting old file");
			f.delete();
		}
		return FileUtil.saveByteArrayToFile(f, content);

	}

	public File[] loadFiles() {
		LOGGER.info("Loading Files | extension:'" + extension + "' path: [" + path + "]");
		File f = new File(path);
		File[] files = f.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(extension);
			}
		});
		return files;

	}

	public void tearDown() {
		directoryWatcher.stop();
	}

}
