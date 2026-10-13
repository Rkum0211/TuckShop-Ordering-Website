package com.tuckshop.ordering.service;

import com.tuckshop.ordering.entity.Product;
import com.tuckshop.ordering.exception.ResourceNotFoundException;
import com.tuckshop.ordering.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(int page, int size, String category, Boolean available, String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return productRepository.findFilteredProducts(category, available, search, pageable);
    }

    @Transactional(readOnly = true)
    public List<Product> getAvailableProducts() {
        return productRepository.findAvailableProducts();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product updatedProduct) {
        Product existingProduct = getProductById(id);
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setDiscountedPrice(updatedProduct.getDiscountedPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        existingProduct.setMaxQuantityPerOrder(updatedProduct.getMaxQuantityPerOrder());
        existingProduct.setAvailable(updatedProduct.getAvailable());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
        return productRepository.save(existingProduct);
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return productRepository.findDistinctCategories();
    }

    @Transactional
    public Product updateStock(Long id, Integer stock) {
        Product existingProduct = getProductById(id);
        existingProduct.setStockQuantity(stock);
        if (stock <= 0) {
            existingProduct.setAvailable(false);
        }
        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setAvailable(false); // Soft delete / unavailable
        productRepository.save(product);
    }
}
