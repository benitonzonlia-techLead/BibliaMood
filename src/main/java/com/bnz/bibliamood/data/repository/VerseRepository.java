package com.bnz.bibliamood.data.repository;

import com.bnz.bibliamood.data.entity.Verse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerseRepository extends JpaRepository<Verse, Long> {
	Page<Verse> findAllByLanguageOrderByIdAsc(String language, Pageable pageable);
	Page<Verse> findAllByOrderByIdAsc(Pageable pageable);
}
