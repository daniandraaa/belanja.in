package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.Order;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.repository.OrderRepository;
import com.ecommerce.ecommerce.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/buyer/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    public OrderController(OrderRepository orderRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        System.out.println("Mengambil semua order...");
        return orderRepo.findAll();
    }


    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderRepo.save(order);
    }

    @PostMapping("/{id}/upload-proof")
    public String uploadProof(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        // Simpan file ke folder "uploads" di root project
        String uploadDir = new File("uploads").getAbsolutePath();  // bikin absolute path

        File folder = new File(uploadDir);
        if (!folder.exists()) folder.mkdirs();  // buat folder kalau belum ada

        String filename = file.getOriginalFilename().replaceAll("\\s+", "_");
        File destination = new File(folder, filename);

        file.transferTo(destination);  // simpan file

        // Update order
        Order order = orderRepo.findById(id).orElseThrow();
        order.setProofPath("/uploads/" + filename); // bisa untuk preview nanti
        order.setStatus("Dibayar");
        orderRepo.save(order);

        return "Bukti berhasil diupload";
    }


    @PutMapping("/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepo.findById(id).orElseThrow();
        order.setStatus(status);
        return orderRepo.save(order);
    }
}
