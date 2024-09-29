package demo.controller;

import demo.dto.BookDTO;
import demo.response.ApiResponse;
import demo.service.BookService;
import demo.utils.ApiResponseUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    // Get all books
    @GetMapping
    @PreAuthorize("isAuthenticated()")  // Require authentication to access this endpoint
    public ResponseEntity<ApiResponse<List<BookDTO>>> getAllBooks() {
        logger.info("Fetching all books");
        List<BookDTO> books = bookService.getAllBooks();
        return ResponseEntity.ok(ApiResponseUtil.success("Books retrieved successfully", books));
    }

    // Get book by ID
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")  // Require authentication
    public ResponseEntity<ApiResponse<BookDTO>> getBookById(@PathVariable UUID id) {
        BookDTO book = bookService.getBookById(id);
        if (book == null) {
            return ResponseEntity.status(404).body(ApiResponseUtil.notFound("Book not found"));
        }
        return ResponseEntity.ok(ApiResponseUtil.success("Book retrieved successfully", book));
    }

    // Create book from JSON
    @PostMapping(consumes = "application/json")
    @PreAuthorize("isAuthenticated()")  // Require authentication
    public ResponseEntity<ApiResponse<BookDTO>> createBookFromJson(@RequestBody BookDTO bookDTO) {
        BookDTO newBook = bookService.createBook(bookDTO, bookDTO.getCategoryId(), null, null);
        return ResponseEntity.ok(ApiResponseUtil.success("Book created successfully", newBook));
    }

    // Create book from form data
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")  // Require authentication
    public ResponseEntity<ApiResponse<BookDTO>> createBookFromFormData(
            @RequestPart("book") String bookDTOString,
            @RequestParam UUID categoryId,
            @RequestPart(value = "file1", required = false) MultipartFile file1,
            @RequestPart(value = "file2", required = false) MultipartFile file2) {
        
        ObjectMapper objectMapper = new ObjectMapper();
        BookDTO bookDTO;
        
        try {
            // Convert JSON string to BookDTO
            bookDTO = objectMapper.readValue(bookDTOString, BookDTO.class);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(ApiResponseUtil.error("Invalid book data", 400));
        }

        // Proceed with creating the book
        BookDTO newBook = bookService.createBook(bookDTO, categoryId, file1, file2);
        return ResponseEntity.ok(ApiResponseUtil.success("Book created successfully", newBook));
    }

    // Update an existing book
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")  // Require authentication
    public ResponseEntity<ApiResponse<BookDTO>> updateBook(@PathVariable UUID id, @RequestBody BookDTO updatedBook) {
        UUID category = updatedBook.getCategoryId() != null ? updatedBook.getCategoryId() : null;
        BookDTO book = bookService.updateBook(id, updatedBook, category);
        return ResponseEntity.ok(ApiResponseUtil.success("Book updated successfully", book));
    }

    // Delete a book
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")  // Require authentication
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);  // Ensure this method is called with the correct ID
        return ResponseEntity.ok(ApiResponseUtil.success("Book deleted successfully", null));
    }
}