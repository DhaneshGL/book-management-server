package com.dhanesh.book.book;


import com.dhanesh.book.common.PageResponse;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookRequest bookRequest;
    private BookResponse bookResponse;
    private BorrowedBookResponse borrowedBookResponse;

    @BeforeEach
    void setUp() {
        bookRequest = new BookRequest(
                null,
                "Test Book",
                "Test Author",
                "123456789",
                "Test Synopsis",
                true
        );


        bookResponse = BookResponse.builder()
                .id(1)
                .title("Test Book")
                .authorName("Test Author")
                .isbn("123456789")
                .synopsis("Test Synopsis")
                .shareable(true)
                .archived(false)
                .build();

        borrowedBookResponse = BorrowedBookResponse.builder()
                .id(1)
                .title("Borrowed Book")
                .authorName("Author")
                .isbn("987654321")
                .returned(false)
                .returnApproved(false)
                .build();
    }

    @Test
    @WithMockUser
    void shouldSaveBook() throws Exception {
        // Given
        when(bookService.save(any(BookRequest.class), any())).thenReturn(1);

        // When & Then
        mockMvc.perform(post("/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void shouldFindBookById() throws Exception {
        // Given
        when(bookService.findById(1)).thenReturn(bookResponse);

        // When & Then
        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    @WithMockUser
    void shouldFindAllBooks() throws Exception {
        // Given
        List<BookResponse> books = Arrays.asList(bookResponse);
        PageResponse<BookResponse> pageResponse = new PageResponse<>(books, 0, 10, 1L, 1, true, true);
        when(bookService.findAllBooks(anyInt(), anyInt(), any())).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/books")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Book"));
    }

    @Test
    @WithMockUser
    void shouldFindAllBooksByOwner() throws Exception {
        // Given
        List<BookResponse> books = Arrays.asList(bookResponse);
        PageResponse<BookResponse> pageResponse = new PageResponse<>(books, 0, 10, 1L, 1, true, true);
        when(bookService.findAllBooksByOwner(anyInt(), anyInt(), any())).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/books/owner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Book"));
    }

    @Test
    @WithMockUser
    void shouldFindAllBorrowedBooks() throws Exception {
        // Given
        List<BorrowedBookResponse> books = Arrays.asList(borrowedBookResponse);
        PageResponse<BorrowedBookResponse> pageResponse = new PageResponse<>(books, 0, 10, 1L, 1, true, true);
        when(bookService.findAllBorrowedBooks(anyInt(), anyInt(), any())).thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/books/borrowed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Borrowed Book"));
    }

    @Test
    @WithMockUser
    void shouldUpdateShareableStatus() throws Exception {
        // Given
        when(bookService.updateShareableStatus(anyInt(), any())).thenReturn(1);

        // When & Then
        mockMvc.perform(patch("/books/shareable/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockUser
    void shouldBorrowBook() throws Exception {
        // Given
        when(bookService.borrowBook(anyInt(), any())).thenReturn(1);

        // When & Then
        mockMvc.perform(post("/books/borrow/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockUser
    void shouldReturnBorrowedBook() throws Exception {
        // Given
        when(bookService.returnBorrowedBook(anyInt(), any())).thenReturn(1);

        // When & Then
        mockMvc.perform(patch("/books/borrow/return/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockUser
    void shouldUploadBookCover() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile("file", "cover.jpg", "image/jpeg", "test content".getBytes());
        doNothing().when(bookService).uploadBookCoverPicture(any(), any(), anyInt());

        // When & Then
        mockMvc.perform(multipart("/books/cover/1")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isAccepted());
    }
}