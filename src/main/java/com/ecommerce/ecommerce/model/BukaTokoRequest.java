//package com.ecommerce.ecommerce.model;
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Pattern;
//import jakarta.validation.constraints.Size;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class BukaTokoRequest {
//
//    @NotBlank(message = "Nama toko tidak boleh kosong")
//    @Size(max = 100, message = "Nama toko maksimal 100 karakter")
//    private String storeName;
//
//    @NotBlank(message = "Username toko tidak boleh kosong")
//    @Size(min = 3, max = 30, message = "Username toko harus antara 3-30 karakter")
//    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username toko hanya boleh berisi huruf, angka, dan underscore")
//    private String storeUsername;
//
//    @Size(max = 1000, message = "Deskripsi toko maksimal 1000 karakter")
//    private String storeDescription; // Deskripsi bisa opsional, jadi @NotBlank tidak ada
//
//    @NotBlank(message = "Nomor telepon toko tidak boleh kosong")
//    @Size(max = 20, message = "Nomor telepon maksimal 20 karakter")
//    @Pattern(regexp = "^\\+?[0-9. ()-]{7,20}$", message = "Format nomor telepon tidak valid")
//    private String storePhoneNumber;
//}
