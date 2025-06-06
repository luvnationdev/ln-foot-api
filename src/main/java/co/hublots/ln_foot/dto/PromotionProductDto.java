package co.hublots.ln_foot.dto;

import java.time.LocalDate;

import co.hublots.ln_foot.models.ProductVariant;
import co.hublots.ln_foot.models.PromotionProduct;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PromotionProductDto {
    private String id;

    @NotNull(message = "Product ID is required")
    private String productVariantId;

    @NotNull(message = "Discounted price is required")
    @Positive(message = "Discounted price must be positive")
    private double discountedPrice;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    public static PromotionProductDto fromEntity(PromotionProduct PromotionProduct) {
        return PromotionProductDto.builder()
                .id(PromotionProduct.getId())
                .productVariantId(PromotionProduct.getProductVariant().getId())
                .discountedPrice(PromotionProduct.getDiscountedPrice())
                .startDate(PromotionProduct.getStartDate())
                .endDate(PromotionProduct.getEndDate())
                .build();
    }

    public PromotionProduct toEntity() {
        return PromotionProduct.builder()
                .id(id)
                .productVariant(ProductVariant.builder().id(productVariantId).build())
                .discountedPrice(discountedPrice)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
    
}       
