package com.ecommerce.ecommerce.model;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String buyerName;
    private int quantity;
    private String notes;
    private String status = "Menunggu Pembayaran";
    private String proofPath;

    @ManyToOne
    private Product product;

    // Getter & Setter wajib
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProofPath() { return proofPath; }
    public void setProofPath(String proofPath) { this.proofPath = proofPath; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
