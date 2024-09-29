package demo.service;

import demo.dto.BookDTO;
import demo.model.Book;
import demo.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.nio.file.Path;

import demo.model.Category;
import demo.repository.CategoryRepository;

@Service
public class BookService {
    private final Path root = Paths.get("uploads");

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public BookService() {
        try {
            Files.createDirectories(root);
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    // Convert Book to DTO
    private BookDTO convertToDTO(Book book) {
        String categoryName = book.getCategory() != null ? book.getCategory().getName() : null;
        return new BookDTO(book.getId(), book.getTitle(), book.getAuthor(), categoryName);
    }

    // Get all books
    public List<BookDTO> getAllBooks() {
        List<Book> books = bookRepository.findAll();  // Ensure this returns books from the DB
        return books.stream().map(this::convertToDTO).collect(Collectors.toList());  // Converts each book to BookDTO
    }    

    // Get book by ID
    public BookDTO getBookById(UUID id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found"));  // Make sure this throws if the book is not found
        return convertToDTO(book);  // Converts the fetched book to BookDTO
    }    
    
    // Create book
    public BookDTO createBook(BookDTO bookDTO, UUID categoryId, MultipartFile imageFile, MultipartFile videoFile) {
        Book book = new Book(bookDTO.getTitle(), bookDTO.getAuthor());

        // Fetch and set the category
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Category not found"));
        book.setCategory(category); // Set the category
    
        try {
            // Check if the image file exists and is not empty
            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = this.storeFile(imageFile);
                book.setImagePath(imagePath); // Set the image path
            }
    
            // Check if the video file exists and is not empty
            if (videoFile != null && !videoFile.isEmpty()) {
                String videoPath = this.storeFile(videoFile);
                book.setVideoPath(videoPath); // Set the video path
            }
    
            // Save the book entity in the database
            Book savedBook = bookRepository.save(book);
            return convertToDTO(savedBook);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save book", e);
        }
    }     

    // Update book
    public BookDTO updateBook(UUID bookId, BookDTO updatedBookDTO, UUID categoryId) {
        // Fetch the existing book
        Book existingBook = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found"));
    
        // Only update the category if categoryId is provided, otherwise keep the existing one
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
            existingBook.setCategory(category); // Update the category
        }
    
        // Update other book fields
        existingBook.setTitle(updatedBookDTO.getTitle());
        existingBook.setAuthor(updatedBookDTO.getAuthor());
    
        // Save the updated book
        Book savedBook = bookRepository.save(existingBook);
    
        // Convert the saved entity to DTO and return
        return convertToDTO(savedBook);
    }
    
    // Delete book
    public void deleteBook(UUID id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Book not found"));  // Make sure this throws if no book is found
        bookRepository.delete(book);  // Properly deletes the book from the repository
    }    

    private String storeFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), this.root.resolve(fileName));
        return this.root.resolve(fileName).toString();
    }
}
