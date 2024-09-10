package com.cbe.lms.lease.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cbe.lms.lease.Model.FileEntity;

@Repository
public interface FileEntityRepository extends JpaRepository<FileEntity, Long> {
    // You can add custom query methods if needed
}
