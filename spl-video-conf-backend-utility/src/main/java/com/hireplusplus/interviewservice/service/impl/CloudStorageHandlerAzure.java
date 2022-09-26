package com.hireplusplus.interviewservice.service.impl;

import com.hireplusplus.interviewservice.utils.CloudStorageUtil;
import com.hireplusplus.interviewservice.utils.MemoryFile;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Service to cater file upload and download to FTP.
 * 
 * @author V V V Durga Prasad
 */
@Service
public class CloudStorageHandlerAzure {

	@Autowired
	public CloudStorageUtil cloudStorageUtil;
	
	@Autowired
	public MessageSource messageSource;

	public static final String AZURE_STORAGE_ENABLED = "on";
	
	public static final String ERROR_TITLE = "error.cloudstorage.download.title";

	private final Logger logger = LoggerFactory.getLogger(CloudStorageHandlerAzure.class);
	
	/**
	 * get the storage account and make call to actual upload to cloud
	 * @param inputStream
	 * @param folderName
	 * @param fileName
	 * @param length
	 * @return true or false
	 */
	public boolean uploadFileToCloudStorage(InputStream inputStream, String folderName, String fileName, long length,
			String connectionString, String containerName){
		boolean uploadStatusFlag = false;
		logger.info("upload {} To Azure Storage Account ",fileName);
		CloudBlobClient cloudBlobClient = cloudStorageUtil.getCloudBlobClient(connectionString);
		uploadStatusFlag = upload(inputStream, folderName, fileName, length, cloudBlobClient, containerName);
		logger.info("File upload status {}",uploadStatusFlag);
		return uploadStatusFlag;
	}

	/**
	 * Upload to cloud
	 * 
	 * @param inputStream
	 * @param folderName
	 * @param fileName
	 * @param length
	 * @param blobClient
	 * @return
	 */
	public boolean upload(InputStream inputStream, String folderName, String fileName, long length,
			CloudBlobClient blobClient, String containerName) {
		CloudBlobContainer container = null;
		CloudBlobDirectory directory = null;
		CloudBlockBlob blob = null;
		logger.info("upload the file to the container{}",containerName);
		if (!ObjectUtils.isEmpty(blobClient)) {
			String azureContainerName = containerName;
			container = getContainer(blobClient,azureContainerName);
		}
		else
			return false;
		
		if (!ObjectUtils.isEmpty(container))
			directory = getDirectory(container,folderName);
		else
			return false;

		if (!ObjectUtils.isEmpty(directory))
			blob = getBlob(directory, fileName);
		else
			return false;

		if (!ObjectUtils.isEmpty(blob))
			upload(blob,inputStream,length);
		else
			return false;
		
		return true;
	}

	/**
	 * Fetch storage Account and make call to cloud storage to download
	 * @param folderName
	 * @return
	 */
	public List<MemoryFile> downloadFromCloudStorage(String folderName, String connectionString,
													 String containerName){
		CloudBlobClient cloudBlobClient = cloudStorageUtil.getCloudBlobClient(connectionString);
		return download(folderName, cloudBlobClient, containerName);
	}

	/**
	 * Download from cloud
	 * @param folderName
	 * @param containerName
	 * @param blobClient
	 * @return
	 */
	public List<MemoryFile> download(String folderName, CloudBlobClient blobClient, String containerName){
		CloudBlobContainer container = null;
		CloudBlobDirectory directory = null;
		List<MemoryFile> memoryFiles = new ArrayList<>();
		
		if (!ObjectUtils.isEmpty(blobClient))
			container = getContainer(blobClient,containerName);
		
		if (!ObjectUtils.isEmpty(container))
			directory = getDirectory(container,folderName);
		
		if (!ObjectUtils.isEmpty(directory))
			memoryFiles = startDownloadProcess(directory);
		
		return memoryFiles;
	}
	
	/**
	 * Download files.
	 *
	 * @param folderName the folder name
	 * @param filenames  the filenames
	 * @return the list
	 */
	public List<MemoryFile> downloadFiles(String folderName, List<String> filenames, String connectionString,
			String containerName) {
		List<MemoryFile> memoryFiles = new ArrayList<>();
		CloudBlobDirectory directory = getDirectory(folderName, connectionString, containerName);
		if (!ObjectUtils.isEmpty(directory)) {
			for (String fileName : filenames) {
				logger.debug("Downlaoding the file {}", fileName);
				CloudBlockBlob blob = getBlob(directory, fileName);
				if (!ObjectUtils.isEmpty(blob)) {
					try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
						blob.download(outStream);
						byte[] batchTextFile = outStream.toByteArray();
						MemoryFile memoryFile = new MemoryFile();
						memoryFile.setContents(batchTextFile);
						memoryFile.setFileName(fileName);
						memoryFiles.add(memoryFile);
					} catch (StorageException | IOException e) {
						// Handle these two exceptions properly
						return null;
					}
				}
			}
		}
		return memoryFiles;
	}


	public List<Boolean> deleteFiles(String folderName, List<String> filenames, String connectionString,
										  String containerName) {
		List<Boolean> statusList = new ArrayList<>();
		CloudBlobDirectory directory = getDirectory(folderName, connectionString, containerName);
		if (!ObjectUtils.isEmpty(directory)) {
			for (String fileName : filenames) {
				logger.debug("deleting the file {}", fileName);
				CloudBlockBlob blob = getBlob(directory, fileName);
				if (!ObjectUtils.isEmpty(blob)) {
					try  {
						statusList.add(blob.deleteIfExists());

					} catch (StorageException  e) {
						// Handle these two exceptions properly
						return null;
					}
				}
			}
		}
		return statusList;
	}

	/**
	 * Gets the directory.
	 *
	 * @param folderName the folder name
	 * @return the directory
	 */
	public CloudBlobDirectory getDirectory(String folderName, String connectionString, String containerName){
		CloudBlobClient blobClient = cloudStorageUtil.getCloudBlobClient(connectionString);
		CloudBlobContainer container = getContainer(blobClient, containerName);
		return getDirectory(container, folderName);
	}

	/**
	 * fetch container reference of an existing container in cloud
	 * @param blobClient
	 * @param azureContainerName
	 * @return
	 */
	public CloudBlobContainer getContainer(CloudBlobClient blobClient,String azureContainerName) {
		try {
			logger.info("get the container reference for the containerName{}",azureContainerName);
			return blobClient.getContainerReference(azureContainerName);
		} catch (URISyntaxException | StorageException e) {
			// Handling these two exception and throw our custom exception and throw it client with proper message

//			logger.error(messageSource.getMessage("error.cloudstorage.container.notavailable.title", null, LocaleContextHolder.getLocale()), e);
//			throw new Covid19AzureCloudStorageException(messageSource.getMessage("error.cloudstorage.container.notavailable.title", null, LocaleContextHolder.getLocale()),
//					messageSource.getMessage("error.cloudstorage.container.notavailable.message", null, LocaleContextHolder.getLocale()));
			return null;
		}
	}
	
	/**
	 * creates and fetch directory from cloud
	 * @param container
	 * @param batchId
	 * @return
	 */
	public CloudBlobDirectory getDirectory(CloudBlobContainer container,String batchId) {
		try {
			return container.getDirectoryReference(batchId);
		} catch (URISyntaxException e) {
			// Handling these two exception and throw our custom exception and throw it client with proper message

//			logger.error(messageSource.getMessage("error.cloudstorage.container.parse.title", null, LocaleContextHolder.getLocale()), e);
//			throw new Covid19AzureCloudStorageException(messageSource.getMessage("error.cloudstorage.container.parse.title", null, LocaleContextHolder.getLocale()),
//				messageSource.getMessage("error.cloudstorage.container.parse.message", null, LocaleContextHolder.getLocale()));
			return null;
//
		}
	}
	
	/**
	 * fetch blob to upload
	 * @param directory
	 * @param fileName
	 * @return
	 */
	public CloudBlockBlob getBlob(CloudBlobDirectory directory,String fileName) {
		try {
			return directory.getBlockBlobReference(fileName);
		} catch (URISyntaxException | StorageException e) {
//			logger.error(messageSource.getMessage("error.cloudstorage.directory.notavailable.title", null, LocaleContextHolder.getLocale()), e);
//			throw new Covid19AzureCloudStorageException(messageSource.getMessage("error.cloudstorage.directory.notavailable.title", null, LocaleContextHolder.getLocale()),
//					messageSource.getMessage("error.cloudstorage.directory.notavailable.message", null, LocaleContextHolder.getLocale()));
			return null;
		}
	}
	
	/**
	 * upload to cloud storage
	 * @param blob
	 * @param inputStream
	 * @param length
	 */
	public void upload(CloudBlockBlob blob,InputStream inputStream,long length) {
		try {
			blob.upload(inputStream, length);
		} catch (StorageException | IOException e) {
//			logger.error(messageSource.getMessage("error.cloudstorage.upload.title", null, LocaleContextHolder.getLocale()), e);
//			throw new Covid19AzureCloudStorageException(messageSource.getMessage("error.cloudstorage.upload.title", null, LocaleContextHolder.getLocale()),
//					messageSource.getMessage("error.cloudstorage.upload.message", null, LocaleContextHolder.getLocale()));
		}
	}
	
	/**
	 * Download process
	 * @param directory
	 * @return
	 */
	public List<MemoryFile> startDownloadProcess(CloudBlobDirectory directory) {
		List<MemoryFile> memoryFiles = new ArrayList<>();
		try {
			for (ListBlobItem blobItem : directory.listBlobs()) {
				if (blobItem instanceof CloudBlobDirectory) {
					logger.info("Ignoring Directory");
					continue;
				}
				String prefix = blobItem.getParent().getPrefix();
				String path = blobItem.getStorageUri().getPrimaryUri().getPath();
				String fileName = path.split(prefix)[1];
				CloudBlockBlob blob = getBlob(directory, fileName);

				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				if (!ObjectUtils.isEmpty(blob)) {
					blob.download(outStream);
					byte[] batchTextFile = outStream.toByteArray();
					MemoryFile memoryFile = new MemoryFile();
					memoryFile.setContents(batchTextFile);
					memoryFile.setFileName(fileName);
					memoryFiles.add(memoryFile);
				}
			}
		} catch (URISyntaxException | StorageException e) {
			logger.error(messageSource.getMessage("error.cloudstorage.container.parse.title", null, LocaleContextHolder.getLocale()), e);
//			throw new Covid19AzureCloudStorageException(messageSource.getMessage("error.cloudstorage.container.parse.title", null, LocaleContextHolder.getLocale()),
//					messageSource.getMessage("error.cloudstorage.container.parse.message", null, LocaleContextHolder.getLocale()));
//			logger.error(messageSource.getMessage(ERROR_TITLE, null, LocaleContextHolder.getLocale()), e);
//			throw new Covid19AzureCloudStorageException(
//					messageSource.getMessage(ERROR_TITLE, null, LocaleContextHolder.getLocale()),
//					messageSource.getMessage("error.cloudstorage.download.message", null, LocaleContextHolder.getLocale()));
		}
		return memoryFiles;
	}
}
