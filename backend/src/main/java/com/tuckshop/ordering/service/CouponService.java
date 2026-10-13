package com.tuckshop.ordering.service;

import com.tuckshop.ordering.entity.Coupon;
import com.tuckshop.ordering.exception.ResourceNotFoundException;
import com.tuckshop.ordering.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional(readOnly = true)
    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        coupon.setCode(coupon.getCode().toUpperCase());
        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon updateCoupon(Long id, Coupon updatedCoupon) {
        Coupon existingCoupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        existingCoupon.setCode(updatedCoupon.getCode().toUpperCase());
        existingCoupon.setDiscountType(updatedCoupon.getDiscountType());
        existingCoupon.setDiscountValue(updatedCoupon.getDiscountValue());
        existingCoupon.setMinimumOrderAmount(updatedCoupon.getMinimumOrderAmount());
        existingCoupon.setMaximumDiscount(updatedCoupon.getMaximumDiscount());
        existingCoupon.setExpiryDate(updatedCoupon.getExpiryDate());
        existingCoupon.setActive(updatedCoupon.getActive());
        return couponRepository.save(existingCoupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        couponRepository.delete(coupon);
    }
}
