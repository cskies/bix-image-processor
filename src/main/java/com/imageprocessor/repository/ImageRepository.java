package com.imageprocessor.repository;

import com.imageprocessor.model.Image;
import com.imageprocessor.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Page<Image> findByOwner(User owner, Pageable pageable);
    List<Image> findByOwner(User owner);
    Optional<Image> findByIdAndOwner(Long id, User owner);
    long countByOwner(User owner);
}