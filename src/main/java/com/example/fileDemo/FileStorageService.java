package com.example.fileDemo;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.fileDemo.exception.FileStorageException;
import com.example.fileDemo.exception.MyFileNotFoundException;

@Service
public class FileStorageService {
	private final Path fileStorageLocation;
	
	@Autowired
	public FileStorageService(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
				.toAbsolutePath().normalize();
		
		try{
			Files.createDirectories(fileStorageLocation);
		}
		catch( Exception e ) {
			throw new FileStorageException("Could not create a directory for file storage.");
		}
	}
	
	public String storeFile( MultipartFile file ) {
		String fileName = StringUtils.cleanPath( file.getOriginalFilename() );
		
		try {
			if( fileName.contains("..") ) {
				throw new FileStorageException("Filename contains invalid file path sequence");
			}
			
			Path targetLocation = this.fileStorageLocation.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			
			return fileName;
		}
		catch( Exception e ) {
			throw new FileStorageException("Could not store file " + fileName + ". Please try again!", e);
        }
	}
	
	public Resource loadFileAsResource( String fileName ) {
		try{
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource( filePath.toUri() );
			
			if( resource.exists() ) {
				return resource;
			}
			else {
				throw new MyFileNotFoundException("File not found : "+ fileName);
			}
		}
		catch( MalformedURLException e ) {
			throw new MyFileNotFoundException("File not found : "+ fileName, e);
		}
	}
	
	public List<String> listAllFiles() {
		try {
			System.out.println(fileStorageLocation);
			List<String> list = new ArrayList<String>();
			Files.newDirectoryStream(fileStorageLocation).forEach(location -> list.add(location.getFileName().toString()) );//.collect( Collectors.toList() );;
			return list;
		}
		catch ( Exception e ) {
			throw new FileStorageException("No directory found : "+fileStorageLocation,e);
		}
	}
}
