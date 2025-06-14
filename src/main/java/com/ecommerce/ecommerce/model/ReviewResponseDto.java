package com.ecommerce.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {
    private Long id;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewDate;
    private String username;        // Nama pengguna yang review
    private Long productId;
    private String productName;
    // Anda bisa tambahkan field lain jika perlu, misal user profile picture url
}