package com.example.fileDemo;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class FileController {
	
	private static final Logger logger = LoggerFactory.getLogger(FileController.class);
	
	@Autowired
	private FileStorageService fileStorageService;

	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public String hello() {
		return "Hello";
	}
	
	@RequestMapping(value = "/upload", method = RequestMethod.POST,
			consumes = "multipart/form-data")
	public UploadFileResponse uploadFile(@RequestPart(value="file", required=true) MultipartFile file) {
		String filename = fileStorageService.storeFile(file);
		
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path(filename)
				.toUriString();
		
		return new UploadFileResponse(filename,fileDownloadUri,
				file.getContentType(), file.getSize());
	}
	
	@RequestMapping(value = "/uploadMultipleFiles", method = RequestMethod.POST)
	public List<UploadFileResponse> uploadMultipleFiles( @RequestPart("files") MultipartFile[] files ){
		return Arrays.asList(files)
				.stream()
				.map( file -> uploadFile(file) )
				.collect( Collectors.toList() );
	}
	
	@RequestMapping( value = "/download/{filename}", method = RequestMethod.GET )
	public ResponseEntity<Resource> getFile( @RequestParam("filename") String fileName, HttpServletRequest request ){
		Resource resource = fileStorageService.loadFileAsResource( fileName );
		String contentType = null;
		
		try {
			contentType = request.getServletContext().getMimeType( resource.getFile().getAbsolutePath() );
		}
		catch ( Exception e ){
			logger.info("Cannot determine file type");
		}
		
		if( contentType == null ) {
			contentType = "application/octet-stream";
		}
		
		return ResponseEntity.ok()
				.contentType( MediaType.parseMediaType(contentType) )
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body( resource );
	}
	
	@RequestMapping( value = "/", method = RequestMethod.GET )
	public ResponseEntity<List<String>> listAllFiles() {
			return ResponseEntity.ok().body( fileStorageService.listAllFiles() );
	}
}
