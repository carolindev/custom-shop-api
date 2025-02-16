package com.carol.customshop.controller;

import com.carol.customshop.api.CartApi;
import com.carol.customshop.dto.AddCartItemRequest;
import com.carol.customshop.dto.CartItemResponse;
import com.carol.customshop.dto.CartResponse;
import com.carol.customshop.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CartController implements CartApi {

    CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public ResponseEntity<CartItemResponse> addCartItem(AddCartItemRequest addCartItemRequest) {
        CartItemResponse response = cartService.addItemToCart(addCartItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<CartResponse> getCartItems() {
        CartResponse response = cartService.getCartItems();
        return ResponseEntity.ok(response);
    }
}
