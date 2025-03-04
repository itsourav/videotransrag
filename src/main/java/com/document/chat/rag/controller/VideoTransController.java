package com.document.chat.rag.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.document.chat.rag.ingestion.VideoTransService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class VideoTransController {

    private static final Logger log = LoggerFactory.getLogger(VideoTransController.class);

    private final VideoTransService videoTransService;

    public VideoTransController(VideoTransService videoTransService) {

        this.videoTransService = videoTransService;
    }

    @PostMapping(value = "/transcript")
    public String videoUrl(@RequestBody String linkUrl) {

        log.info("linkUrl is : {} ", linkUrl);
        String withoutQuotes_link = linkUrl.replace("\"", "");
        log.info("withoutQuotes_line1 is : {} ", withoutQuotes_link);
        String transcriptedValue = videoTransService.processing(withoutQuotes_link);
        return transcriptedValue;
    }

    @PostMapping(value = "/saveTranscript")
    public void saveTranscript(@RequestBody String transcript) {
        String updatedValue = "";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode tree = objectMapper.readTree(transcript);
            JsonNode node = tree.get("text");
            updatedValue = node.textValue();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        log.info("Save the updatedValue : {} ", updatedValue);
        //String insertionStatus =  videoTransService.insertDocuments(transcript);
    }

}
