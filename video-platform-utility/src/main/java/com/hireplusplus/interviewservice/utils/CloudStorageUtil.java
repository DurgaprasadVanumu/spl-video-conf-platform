package com.hireplusplus.interviewservice.utils;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageCredentials;
import com.microsoft.azure.storage.StorageCredentialsSharedAccessSignature;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

/**
 * Util for cloud uplaod and download process
 * @author V V V Durga Prasad
 *
 */
@Service
public class CloudStorageUtil {
	
//	@Autowired
//	public MessageSource messageSource;
	
	private final Logger logger = LoggerFactory.getLogger(CloudStorageUtil.class);

//	private static final String TITLE = "error.cloudstorage.connection.title";


	/**
	 * Gets the cloud blob client.
	 *
	 * @param connectionString the connection string
	 * @return the cloud blob client
	 */
	public CloudBlobClient getCloudBlobClient(String connectionString) {
		logger.info("Getting azure bolb storage connection");
		try {
			int index = connectionString.lastIndexOf("?");
			if (index == -1) {
				return CloudStorageAccount.parse(connectionString).createCloudBlobClient();
			} else {
				String[] connectionStringArr = connectionString.split("\\?");
				String storageConnectionString = connectionStringArr[0];
				String sasToken = connectionStringArr[1];
				StorageCredentials creds = new StorageCredentialsSharedAccessSignature(sasToken);
				return new CloudBlobClient(new URI(storageConnectionString), creds);
			}
		} catch (URISyntaxException | InvalidKeyException e) {
			//Handle these exceptions and throw proper custom exception or return valid user readable message to client
//			logger.error(messageSource.getMessage(TITLE, null,
//					LocaleContextHolder.getLocale()), e);
//			throw new Covid19AzureCloudStorageException(
//					messageSource.getMessage(TITLE, null,
//							LocaleContextHolder.getLocale()),
//					messageSource.getMessage("error.cloudstorage.connection.message", null,
//							LocaleContextHolder.getLocale()));
			return null;
		}
	}
	
}
