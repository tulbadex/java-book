package demo.dto;

import java.util.UUID;

public class BookDTO {
    private UUID id;
    private String title;
    private String author;
    private String categoryName;
    
    private UUID categoryId;

    // Constructors
    public BookDTO() {}

    public BookDTO(UUID id, String title, String author, String categoryName) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.author = author;
        this.categoryName = categoryName;
    }

    // Getters and Setters
    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }   
}
