package com.institut.cloudapi.repository;

import com.institut.cloudapi.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
