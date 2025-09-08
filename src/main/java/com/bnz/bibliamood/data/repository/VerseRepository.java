package com.bnz.bibliamood.data.repository;

import com.bnz.bibliamood.data.entity.Verse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerseRepository extends JpaRepository<Verse, Long> {
}
