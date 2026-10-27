package com.exp9.cicd.repository;

import com.exp9.cicd.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStockLessThan(int threshold);
}
