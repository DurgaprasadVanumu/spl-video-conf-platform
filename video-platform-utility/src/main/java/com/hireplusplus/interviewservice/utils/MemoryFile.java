package com.hireplusplus.interviewservice.utils;

import lombok.ToString;

/**
 * The Class MemoryFile.
 * 
 * @author Naveen
 */
@ToString
public class MemoryFile {

	private String fileName;
	private byte[] contents;
	private boolean isManualUpload;
	private String fileLocation;

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the contents
	 */
	public byte[] getContents() {
		return contents;
	}

	/**
	 * @param contents
	 *            the contents to set
	 */
	public void setContents(byte[] contents) {
		this.contents = contents;
	}

	/**
	 * @return the isManualUpload
	 */
	public boolean isManualUpload() {
		return isManualUpload;
	}

	/**
	 * @param isManualUpload the isManualUpload to set
	 */
	public void setManualUpload(boolean isManualUpload) {
		this.isManualUpload = isManualUpload;
	}

	/**
	 * @return the complete folder location of file inside the container
	 */
	public String getFileLocation() {
		return fileLocation;
	}

	/**
	 * @param fileLocation to set file location
	 */
	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}
}