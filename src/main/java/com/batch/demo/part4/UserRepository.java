package com.batch.demo.part4;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Collection<User> findAllByUpdatedDate(LocalDate now);
}
