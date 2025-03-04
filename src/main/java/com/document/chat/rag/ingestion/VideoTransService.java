package com.document.chat.rag.ingestion;

import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import io.github.thoroldvix.api.TranscriptContent;
import io.github.thoroldvix.api.YoutubeTranscriptApi;
import io.github.thoroldvix.internal.TranscriptApiFactory;

@Service
public class VideoTransService {
    

     private static final Logger log = LoggerFactory.getLogger(VideoTransService.class);

     private final VectorStore vectorStore;

      private final TokenTextSplitter tokenSplitter = new TokenTextSplitter();


      

     public VideoTransService(VectorStore vectorStore) {

        this.vectorStore = vectorStore;
    }


    public String processing(String link) {
        try {
            String videoId = link.split("v=")[1];
            log.info("Video ID : {}", videoId);

            YoutubeTranscriptApi youtubeTranscriptApi = TranscriptApiFactory.createDefault();
            TranscriptContent transcriptContent = youtubeTranscriptApi.listTranscripts(videoId)
                    .findTranscript("en")
                    .fetch();

            if (Objects.nonNull(transcriptContent) && Objects.nonNull(transcriptContent.getContent()) && !transcriptContent.getContent().isEmpty()) {
                //createCollection(videoId);

                String content = transcriptContent.getContent()
                        .stream()
                        .filter(c -> !c.getText().contains("[") && !c.getText().contains("]"))
                        .map(TranscriptContent.Fragment::getText)
                        .collect(Collectors.joining(" "));
                
               // insertDocuments(content.replace("\n", " "));

                return content;
            } else {
                throw new IllegalStateException(String.format("Video %s doesn't have a transcript", link));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException(String.format("Video %s is not processed due to %s", link, e.getMessage()));
        }
    }


     public String insertDocuments(String content) {
	   
        String videoContent = content.replace("\n", " ");
        String insertionStatus = null;
		// log.info("videoContent : {}", videoContent);
	  try {  
		    Resource textResource = new ByteArrayResource(videoContent.getBytes());
		  
	        TextReader textReader = new TextReader(textResource);
		 
		 // Step 2: Split the content into tokens
	        var tokens = tokenSplitter.split(textReader.read());
	        
	        if (tokens.isEmpty()) {
	            throw new IllegalStateException("No tokens were extracted from the Transcription.");
	        }
	        
	        // Step 3: Write tokens to the vector store
	        vectorStore.write(tokens);
	        
	        // Log success for debugging
	        log.info("Document successfully uploaded to vector store!");
	        log.info("Tokens count: {}", tokens.size());
            insertionStatus = "success";
	      } catch (Exception e) {
            insertionStatus = "Failed";
	        throw new RuntimeException("Failed to load document: " + e.getMessage(), e);
	     }
		 
         return insertionStatus;
		 
	   }

}
