package com.dhanesh.book.feedback;


import com.dhanesh.book.common.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(FeedbackController.class)
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedbackService feedbackService;

    @Autowired
    private ObjectMapper objectMapper;

    private FeedbackRequest feedbackRequest;
    private FeedbackResponse feedbackResponse;
    @BeforeEach
    void setUp() {
        feedbackRequest = new FeedbackRequest(
            4.5,
            "Really enjoyed reading this book", 
            1 
        );

        feedbackResponse = new FeedbackResponse(
            4.5, 
            "Really enjoyed reading this book",
            true 
        );
    }



    @Test
    @WithMockUser
    void shouldSaveFeedback() throws Exception {
        // Given
        when(feedbackService.save(any(FeedbackRequest.class), any())).thenReturn(1);

        // When & Then
        mockMvc.perform(post("/feedbacks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockUser
    void shouldFindAllFeedbacksByBook() throws Exception {
        // Given
        List<FeedbackResponse> feedbacks = Arrays.asList(feedbackResponse);
        PageResponse<FeedbackResponse> pageResponse = new PageResponse<>(
                feedbacks, 0, 10, 1L, 1, true, true);
        when(feedbackService.findAllFeedbacksByBook(anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/feedbacks/book/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].note").value("Great book!"))
                .andExpect(jsonPath("$.content[0].comment").value("Really enjoyed reading this book"));
    }

    @Test
    @WithMockUser
    void shouldFindAllFeedbacksByBookWithDefaultPagination() throws Exception {
        // Given
        List<FeedbackResponse> feedbacks = Arrays.asList(feedbackResponse);
        PageResponse<FeedbackResponse> pageResponse = new PageResponse<>(
                feedbacks, 0, 10, 1L, 1, true, true);
        when(feedbackService.findAllFeedbacksByBook(anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/feedbacks/book/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidFeedback() throws Exception {
        // Given
        FeedbackRequest invalidRequest = new FeedbackRequest(
            null,        
            "",         
            null     
        );


        // When & Then
        mockMvc.perform(post("/feedbacks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldFindFeedbacksWithCustomPagination() throws Exception {
        // Given
        List<FeedbackResponse> feedbacks = Arrays.asList(feedbackResponse);
        PageResponse<FeedbackResponse> pageResponse = new PageResponse<>(
                feedbacks, 1, 5, 1L, 1, false, true);
        when(feedbackService.findAllFeedbacksByBook(eq(1), eq(1), eq(5), any()))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/feedbacks/book/1")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5));
    }
}