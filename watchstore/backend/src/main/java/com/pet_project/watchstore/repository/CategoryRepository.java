package com.pet_project.watchstore.repository;

import com.pet_project.watchstore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByOrderByNameAsc();
}
