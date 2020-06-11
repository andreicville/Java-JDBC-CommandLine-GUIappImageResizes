package com.example.filesearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileSearchApp {
	String path;
	String regex;
	String zipFileName;
	Pattern pattern;
	List<File> zipFiles = new ArrayList<File>();

	public static void main(String[] args) {
		for (String arg : args) {
			System.out.println(arg);
		}
		FileSearchApp app = new FileSearchApp();
		switch (Math.min(args.length, 3)) {
		case 0:
			System.out.println("USAGE: FIleSearchApp path [regex] [zifile]");
			return;
		case 3:
			app.setZipFileName(args[2]);
		case 2:
			app.setRegex(args[1]);
		case 1:
			app.setPath(args[0]);
		}
		try {
			app.walkDirectory(app.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void walkDirectory(String path) throws IOException {
		Files.walk(Paths.get(path)).forEach(f -> processFile(f.toFile()));
		zipFiles();

	}

	private void processFile(File file) {
		try {
			if (searchFile(file)) {
				addFileToZip(file);
			}
		} catch (Exception e) {

			System.out.println("Error processing File: " + file + " : " + e);
		}
	}

	private void addFileToZip(File file) {
		if (getZipFileName() != null) {
			zipFiles.add(file);
		}

	}

	public void zipFiles() throws IOException {
		try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(getZipFileName()))) {
			File baseDir = new File(getPath());

			for (File file : zipFiles) {
				// fileName must be relative path
				String fileName = getRelativeFilename(file, baseDir);
				ZipEntry zipEntry = new ZipEntry(fileName);
				zipEntry.setTime(file.lastModified());
				out.putNextEntry(zipEntry);
				Files.copy(file.toPath(), out);
				out.closeEntry();
			}
		}
	}

	private String getRelativeFilename(File file, File baseDir) {
		String fileName = file.getAbsolutePath().substring(baseDir.getAbsolutePath().length());
		fileName = fileName.replace('\\', '/');
		while (fileName.startsWith("/")) {
			fileName = fileName.substring(1);
		}
		return fileName;
	}

	private boolean searchFile(File file) throws Exception {

		return Files.lines(file.toPath(), StandardCharsets.UTF_8).anyMatch(t -> searchText(t));
	}

	private boolean searchText(String text) {
		return (this.getRegex() == null) ? true : this.pattern.matcher(text).find();
	}

	public String getRegex() {

		return null;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setRegex(String regex) {
		this.regex = regex;
		this.pattern = Pattern.compile(regex);
	}

	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

}
