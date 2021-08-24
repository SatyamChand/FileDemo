package com.example.fileDemo.exception;

public class FileStorageException extends RuntimeException {

    /**
	 * autobuilt serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
