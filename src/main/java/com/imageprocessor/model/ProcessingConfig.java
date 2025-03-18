package com.imageprocessor.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "processing_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resize_enabled")
    private boolean resizeEnabled;

    @Column(name = "resize_percentage")
    private Integer resizePercentage;

    @Column(name = "grayscale_enabled")
    private boolean grayscaleEnabled;

    @OneToOne(mappedBy = "processingConfig")
    private ProcessingTask processingTask;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}