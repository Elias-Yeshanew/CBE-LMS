package com.example.demo.lease.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.lease.Model.FileEntity;

@Repository
public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {
    // You can add custom query methods if needed
}
