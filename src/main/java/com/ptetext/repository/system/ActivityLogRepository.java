package com.ptetext.repository.system;

import com.ptetext.entity.system.ActivityLogEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, Long> {

    List<ActivityLogEntity> findTop10ByOrderByLogIdDesc();
}
